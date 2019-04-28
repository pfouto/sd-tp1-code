package microgram.impl.srv.java;

import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.srv.rest.RestResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;

public class JavaProfiles extends RestResource implements Profiles {

    private Map<String, Profile> users = new ConcurrentHashMap<>();
    private Map<String, Set<String>> followers = new ConcurrentHashMap<>();
    private Map<String, Set<String>> following = new ConcurrentHashMap<>();

    private Posts postsClient;

    public JavaProfiles(Posts postsClient) {
        this.postsClient = postsClient;
    }

    @Override
    public Result<Profile> getProfile(String userId) {

        Profile res = users.get(userId);

        if (res == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        res.setFollowers(followers.get(userId).size());
        res.setFollowing(following.get(userId).size());

        return Result.ok(res);
    }

    @Override
    public Result<Void> createProfile(Profile profile) {

        try {
            if (users.putIfAbsent(profile.getUserId(), profile) != null) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }

            followers.put(profile.getUserId(), new HashSet<>());
            following.put(profile.getUserId(), new HashSet<>());

            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Result<Void> deleteProfile(String userId) {
        Profile p = users.remove(userId);
        if (p == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Set<String> myFollowees = following.remove(userId);
        Set<String> myFollowers = followers.remove(userId);

        for (String followee : myFollowees)
            followers.get(followee).remove(userId);

        for (String follower : myFollowers)
            following.get(follower).remove(userId);

        postsClient.purgeProfileActivity(userId);
        return Result.ok();
    }

    @Override
    public Result<List<Profile>> search(String prefix) {
        return Result.ok(users.values().stream()
                                 .filter(p -> p.getUserId().startsWith(prefix))
                                 .collect(Collectors.toList()));
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
        return Result.error(NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> internalFollowFront(String userId1, String userId2, boolean isFollowing) {
        Set<String> s1 = following.get(userId1);

        if (s1 == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if (isFollowing) {
            boolean added1 = s1.add(userId2);
            if (!added1)
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            boolean removed1 = s1.remove(userId2);
            if (!removed1)
                return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Void> internalFollowReverse(String userId1, String userId2, boolean isFollowing) {
        Set<String> s2 = followers.get(userId2);

        if (s2 == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if (isFollowing) {
            boolean added2 = s2.add(userId1);
            if (!added2)
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            boolean removed2 = s2.remove(userId1);
            if (!removed2)
                return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Boolean> isFollowing(String userId1, String userId2) {

        Set<String> s1 = following.get(userId1);
        if (s1 == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);
        else
            return Result.ok(s1.contains(userId2));
    }

    @Override
    public Result<Set<String>> getFollowees(String userId) {
        if (following.containsKey(userId))
            return Result.ok(following.get(userId));
        else
            return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> updateProfile(Profile profile) {
        if (users.containsKey(profile.getUserId())) {
            users.put(profile.getUserId(), profile);
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public Result<Void> updateNumberOfPosts(String userId, boolean increase) {
        try {
            Profile p = users.get(userId);
            if (p != null) {
                if (increase || p.getPosts() > 0) {
                    p.incPosts(increase);
                    return Result.ok();
                }
                return Result.error(Result.ErrorCode.CONFLICT);
            } else {
                return Result.error(Result.ErrorCode.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(INTERNAL_ERROR);
        }
    }

}
