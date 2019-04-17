package microgram.impl.srv.java;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Profile;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.RestResource;

public class JavaProfiles extends RestResource implements microgram.api.java.Profiles {

	Map<String, Profile> users = new ConcurrentHashMap<>();
	Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	Map<String, Set<String>> following = new ConcurrentHashMap<>();

	private Posts postsClient;

	public JavaProfiles(URI postsUri) {
		postsClient = ClientFactory.getPostsClient(postsUri);
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = users.get( userId );
		if( res == null )
			return Result.error(Result.ErrorCode.NOT_FOUND);

		res.setFollowers( followers.get(userId).size() );
		res.setFollowing( following.get(userId).size() );
		return Result.ok(res);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		if( users.putIfAbsent( profile.getUserId(), profile ) != null ) {
			return Result.error(Result.ErrorCode.CONFLICT);
		}

		followers.put( profile.getUserId(), new HashSet<>());
		following.put( profile.getUserId(), new HashSet<>());

		return Result.ok();
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		Profile p = this.users.remove(userId);
		if(p != null) {
			Set<String> followees = this.following.remove(userId);
			Set<String> followers = this.followers.remove(userId);

			for(String follow: followees) {
				this.follow(userId, follow, false);
			}

			for(String follow: followers) {
				this.follow(follow, userId, false);
			}

			Result<List<String>> posts = postsClient.getPosts(userId);

			if(posts.isOK()) {
				for(String post: posts.value()) {
					postsClient.deletePost(post);
				}
			}

			postsClient.unlikeAllPosts(userId);
			return Result.ok();
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return Result.ok(users.values().stream()
				.filter( p -> p.getUserId().startsWith( prefix ) )
				.collect( Collectors.toList()));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		Set<String> s1 = following.get( userId1 );
		Set<String> s2 = followers.get( userId2 );

		if( s1 == null || s2 == null)
			return Result.error(Result.ErrorCode.NOT_FOUND);

		if( isFollowing ) {
			if( ! s1.add( userId2 ) || ! s2.add( userId1 ) )
				return Result.error(Result.ErrorCode.CONFLICT);
		} else {
			if( ! s1.remove( userId2 ) || ! s2.remove( userId1 ) )
				return Result.error(Result.ErrorCode.NOT_FOUND);
		}
		return Result.ok();
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {

		Set<String> s1 = following.get( userId1 );
		Set<String> s2 = followers.get( userId2 );

		if( s1 == null || s2 == null)
			return Result.error(Result.ErrorCode.NOT_FOUND);
		else
			return Result.ok(s1.contains( userId2 ) && s2.contains( userId1 ));
	}

	@Override
	public Result<Set<String>> getFollowees(String userId) {
		if(following.containsKey(userId))
			return Result.ok(following.get(userId));
		else
			return Result.error(Result.ErrorCode.NOT_FOUND);
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		if(users.containsKey(profile.getUserId())) {
			users.put(profile.getUserId(), profile);
			return Result.ok();
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}

	@Override
	public Result<Void> updateNumberOfPosts(String userId, boolean increase) {
		Profile p = users.get(userId);
		if(p != null) {
			if(increase || p.getPosts() > 0) {
				p.incPosts(increase);
				return Result.ok();
			}
			return Result.error(Result.ErrorCode.CONFLICT);
		} else {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}
	}

}
