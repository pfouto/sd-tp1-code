package microgram.impl.srv.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import discovery.Discovery;
import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.MediaRestServer;
import microgram.impl.srv.rest.ProfilesRestServer;
import utils.Hash;

public class JavaPosts implements Posts {

	protected Map<String, Post> posts = new HashMap<>();
	protected Map<String, Set<String>> likes = new HashMap<>();
	protected Map<String, Set<String>> userPosts = new HashMap<>();

	private Map<URI,Posts> postsServers = new HashMap<URI, Posts>();
	private Map<URI,Profiles> profileServers = new HashMap<URI, Profiles>();
	private Map<URI,Media> mediaServers = new HashMap<URI, Media>();

	public JavaPosts(URI[] posts, URI[] profiles, URI[] media) {
		
		for(URI u: posts) {
			postsServers.put(u, ClientFactory.getPostsClient(u));
		}
		
		for(URI u: profiles) {
			profileServers.put(u, ClientFactory.getProfiles(u));
		}
		
		for(URI u: media) {
			mediaServers.put(u, ClientFactory.getMediaClient(u));
		}
	}

	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.get(postId);
		if (res != null)
			return Result.ok(res);
		else
			return Result.error(Result.ErrorCode.NOT_FOUND);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Post p = this.posts.remove(postId);
		if(p == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		this.userPosts.get(p.getOwnerId()).remove(postId);

		this.mediaServers.values().iterator().next().delete(p.getMediaUrl().substring(p.getMediaUrl().lastIndexOf('/')+1));
		
		Profiles pserver = this.profileServers.values().iterator().next();
		
		//TODO: Might be smarter to have a single operation to lower the value of the posts.
		Profile profile = pserver.getProfile(p.getOwnerId()).value();
		
		profile.setPosts(profile.getPosts() - 1);
		
		//TODO:
		//pserver.updateProfile(profile);
		
		return Result.ok();

	}

	@Override
	public Result<String> createPost(Post post) {
		String postId = Hash.of(post.getOwnerId(), post.getMediaUrl());
		if (posts.putIfAbsent(postId, post) == null) {

			likes.put(postId, new HashSet<>());

			Set<String> posts = userPosts.get(post.getOwnerId());
			if (posts == null)
				userPosts.put(post.getOwnerId(), posts = new LinkedHashSet<>());
			posts.add(postId);

			return Result.ok(postId);
		}
		else
			return Result.error(Result.ErrorCode.CONFLICT);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Set<String> res = likes.get(postId);
		if (res == null)
			return Result.error( Result.ErrorCode.NOT_FOUND );

		if (isLiked) {
			if (!res.add(userId))
				return Result.error( Result.ErrorCode.CONFLICT );
		} else {
			if (!res.remove(userId))
				return Result.error( Result.ErrorCode.NOT_FOUND );
		}

		getPost(postId).value().setLikes(res.size());
		return Result.ok();
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Set<String> res = likes.get(postId);

		if (res != null)
			return Result.ok(res.contains(userId));
		else
			return Result.error( Result.ErrorCode.NOT_FOUND );
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return Result.ok(new ArrayList<>(res));
		else
			return Result.error( Result.ErrorCode.NOT_FOUND );
	}


	@Override
	public Result<List<String>> getFeed(String userId) {
		return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
	}
}
