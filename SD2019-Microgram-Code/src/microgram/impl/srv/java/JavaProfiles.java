package microgram.impl.srv.java;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.RestResource;
import utils.ClockedValue;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.*;
import static microgram.api.java.Result.ErrorCode.*;

public class JavaProfiles extends RestResource implements Profiles {

    private Map<String, Map<String, ClockedValue>> userPostsNumber = new ConcurrentHashMap<>();

    private Posts postsClient;

    private static final String USER_KEYSPACE = "users";
    private static final String USER_TABLE = "user_data";
    private static final String USER_COUNTERS_TABLE = "user_counters";

    private final CqlSession session;
    private final PreparedStatement preparedGetUser;
    private final PreparedStatement preparedGetUserCounters;
    private final PreparedStatement preparedCreateUser;
    private final PreparedStatement preparedGetUserFollowing;
    private final PreparedStatement preparedDeleteUser;
    private final PreparedStatement preparedDeleteUserCounter;
    private final PreparedStatement preparedIncreaseFollowers;
    private final PreparedStatement preparedDecreaseFollowers;
    private final PreparedStatement preparedIncreaseFollowings;
    private final PreparedStatement preparedDecreaseFollowings;
    private final PreparedStatement preparedGetUsersFollowing;
    private final PreparedStatement preparedSearchPrefix;
    private final PreparedStatement preparedGetUserCountersIn;
    private final PreparedStatement preparedGetUserIdsIn;
    private final PreparedStatement preparedCheckIfFollowing;
    private final PreparedStatement preparedGetAllUserIds;
    private final PreparedStatement preparedUpdateUser;

    public JavaProfiles(URI postsURI) {
        this.postsClient = ClientFactory.getPostsClient(postsURI);
        try {

            session = CqlSession.builder().withKeyspace(USER_KEYSPACE)
                    //.addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                    //.withLocalDatacenter("dc1")
                    .build();

            preparedGetUser = session.prepare(selectFrom(USER_TABLE).columns("fullName", "photoUrl")
                                                      .whereColumn("userId").isEqualTo(bindMarker()).build());
            preparedGetAllUserIds = session.prepare(
                    selectFrom(USER_TABLE).columns("userId", "fullName", "photoUrl").build());
            preparedGetUserCounters = session.prepare(selectFrom(USER_COUNTERS_TABLE)
                                                              .columns("posts", "following", "followers")
                                                              .whereColumn("userId").isEqualTo(bindMarker()).build());
            preparedCreateUser = session.prepare(insertInto(USER_TABLE)
                                                         .value("userId", bindMarker())
                                                         .value("fullName", bindMarker())
                                                         .value("photoUrl", bindMarker())
                                                         .value("searchable_userId", bindMarker())
                                                         .ifNotExists().build());
            preparedGetUserFollowing = session.prepare(selectFrom(USER_TABLE).columns("following")
                                                               .whereColumn("userId").isEqualTo(bindMarker()).build());
            preparedDeleteUser = session.prepare(deleteFrom(USER_TABLE).whereColumn("userId")
                                                         .isEqualTo(bindMarker()).ifExists().build());
            preparedDeleteUserCounter = session.prepare(deleteFrom(USER_COUNTERS_TABLE).whereColumn("userId")
                                                                .isEqualTo(bindMarker()).build());
            preparedDecreaseFollowers = session.prepare(update(USER_COUNTERS_TABLE).decrement("followers")
                                                                .whereColumn("userId").isEqualTo(bindMarker()).build());
            preparedIncreaseFollowers = session.prepare(update(USER_COUNTERS_TABLE).increment("followers")
                                                                .whereColumn("userId").isEqualTo(bindMarker()).build());
            preparedDecreaseFollowings = session.prepare(update(USER_COUNTERS_TABLE).decrement("following")
                                                                 .whereColumn("userId").isEqualTo(
                            bindMarker()).build());
            preparedIncreaseFollowings = session.prepare(update(USER_COUNTERS_TABLE).increment("following")
                                                                 .whereColumn("userId").isEqualTo(
                            bindMarker()).build());
            preparedGetUsersFollowing = session.prepare(selectFrom(USER_TABLE).column("userId")
                                                                .whereColumn("following").contains(
                            bindMarker()).build());
            preparedSearchPrefix = session.prepare(selectFrom(USER_TABLE).columns("userId", "fullName", "photoUrl")
                                                           .whereColumn("searchable_userId").like(
                            bindMarker()).build());
            preparedGetUserCountersIn = session.prepare(selectFrom(USER_COUNTERS_TABLE)
                                                                .columns("userId", "posts", "following", "followers")
                                                                .whereColumn("userId").in(bindMarker()).build());
            preparedGetUserIdsIn = session.prepare(selectFrom(USER_TABLE).columns("userId")
                                                           .whereColumn("userId").in(bindMarker()).build());
            preparedCheckIfFollowing = session.prepare(selectFrom(USER_TABLE).column("userId")
                                                               .whereColumn("userId").isEqualTo(bindMarker())
                                                               .whereColumn("following").contains(bindMarker())
                                                               .build());
            preparedUpdateUser = session.prepare(update(USER_TABLE).setColumn("fullname", bindMarker())
                                                         .setColumn("photourl", bindMarker())
                                                         .whereColumn("userId").isEqualTo(bindMarker())
                                                         .ifExists().build());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            throw e;
        }
        System.out.println("Built!");
    }

    @Override
    public Result<Profile> getProfile(String userId) {

        Row user = session.execute(preparedGetUser.bind(userId)).one();
        if (user == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);
        Row userCounters = session.execute(preparedGetUserCounters.bind(userId)).one();

        Profile res = new Profile(userId, user.getString("fullName"), user.getString("photoUrl"));
        if (userCounters != null)
            res.setCounters((int) userCounters.getLong("posts"), (int) userCounters.getLong("following"),
                            (int) userCounters.getLong("followers"));

        System.err.println("Get profile: " + userId + " - " + res);
        return Result.ok(res);
    }

    @Override
    public Result<Void> createProfile(Profile p) {
        ResultSet rs = session.execute(
                preparedCreateUser.bind(p.getUserId(), p.getFullName(), p.getPhotoUrl(), p.getUserId()));
        if (!rs.wasApplied())
            return Result.error(Result.ErrorCode.CONFLICT);
        return Result.ok();
    }

    @Override
    public Result<Void> deleteProfile(String userId) {
        //Pre-fetch followings
        Row followingRow = session.execute(preparedGetUserFollowing.bind(userId)).one();

        //Delete user and check if success
        ResultSet execute = session.execute(preparedDeleteUser.bind(userId));
        if (!execute.wasApplied())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        //Delete user counters
        session.execute(preparedDeleteUserCounter.bind(userId));

        //For each following, decrease that user followers counter:
        if (followingRow != null) {
            Set<String> followingSet = followingRow.getSet("following", String.class);
            if (followingSet != null)
                for (String following : followingSet)
                    session.execute(preparedDecreaseFollowers.bind(following));
        }
        //For each user following this one, remove that following and decrease following counter:
        ResultSet usersFollowing = session.execute(preparedGetUsersFollowing.bind(userId));
        for (Row userFollowing : usersFollowing) {
            String userFollowingId = userFollowing.getString("userId");
            session.execute(preparedDecreaseFollowings.bind(userFollowingId));
            session.execute(update(USER_TABLE).removeSetElement("following", literal(userId))
                                    .whereColumn("userId").isEqualTo(literal(userFollowingId)).build());
        }

        //Finally, delete posts and likes
        postsClient.purgeProfileActivity(userId);
        return Result.ok();
    }

    @Override
    public Result<List<Profile>> search(String prefix) {
        //To store results
        Map<String, Profile> resultMap = new HashMap<>();

        //Get data using like%
        ResultSet result = prefix.isEmpty() ? session.execute(preparedGetAllUserIds.bind())
                : session.execute(preparedSearchPrefix.bind(prefix + "%"));
        for (Row user : result) {
            String userId = user.getString("userId");
            resultMap.put(userId, new Profile(userId, user.getString("fullName"), user.getString("photoUrl")));
        }

        //Get counters and update map with retrieved info
        if (!resultMap.isEmpty()) {
            ResultSet counterResults = session.execute(
                    preparedGetUserCountersIn.bind(Arrays.asList(resultMap.keySet().toArray())));
            for (Row userCounters : counterResults) {
                String userId = userCounters.getString("userId");
                resultMap.get(userId).setCounters((int) userCounters.getLong("posts"),
                                                  (int) userCounters.getLong("following"),
                                                  (int) userCounters.getLong("followers"));
            }
        }
        return Result.ok(new ArrayList<>(resultMap.values()));
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
        ResultSet existsResult = session.execute(preparedGetUserIdsIn.bind(Arrays.asList(userId1, userId2)));
        if (existsResult.all().size() < 2)
            return Result.error(NOT_FOUND);

        Row isFollowingResult = session.execute(preparedCheckIfFollowing.bind(userId1, userId2)).one();
        if (isFollowing) {
            if (isFollowingResult != null)
                return Result.error(CONFLICT);
            session.execute(update(USER_TABLE).appendSetElement("following", literal(userId2))
                                    .whereColumn("userId").isEqualTo(literal(userId1)).build());
            session.execute(preparedIncreaseFollowers.bind(userId2));
            session.execute(preparedIncreaseFollowings.bind(userId1));

        } else {
            if (isFollowingResult == null)
                return Result.error(NOT_FOUND);
            session.execute(update(USER_TABLE).removeSetElement("following", literal(userId2))
                                    .whereColumn("userId").isEqualTo(literal(userId1)).build());
            session.execute(preparedDecreaseFollowers.bind(userId2));
            session.execute(preparedDecreaseFollowings.bind(userId1));
        }

        return Result.ok();
    }

    @Override
    public Result<Boolean> isFollowing(String userId1, String userId2) {

        ResultSet existsResult = session.execute(preparedGetUserIdsIn.bind(Arrays.asList(userId1, userId2)));
        if (existsResult.all().size() < 2)
            return Result.error(NOT_FOUND);

        Row isFollowingResult = session.execute(preparedCheckIfFollowing.bind(userId1, userId2)).one();
        return Result.ok(isFollowingResult != null);
    }

    @Override
    public Result<Set<String>> getFollowees(String userId) {
        Row followingRow = session.execute(preparedGetUserFollowing.bind(userId)).one();
        if (followingRow == null)
            return Result.error(NOT_FOUND);
        Set<String> following = followingRow.getSet("following", String.class);
        return following != null ? Result.ok(following) : Result.ok(Collections.emptySet());
    }

    @Override
    public Result<Void> updateProfile(Profile profile) {
        ResultSet execute = session.execute(
                preparedUpdateUser.bind(profile.getFullName(), profile.getPhotoUrl(), profile.getUserId()));
        return execute.wasApplied() ? Result.ok() : Result.error(NOT_FOUND);
    }

    @Override
    public Result<Void> updateNumberOfPosts(String userId, String replica, ClockedValue clockedValue) {
        //Now changed directly by the posts server
        return Result.error(NOT_IMPLEMENTED);
    }
}
