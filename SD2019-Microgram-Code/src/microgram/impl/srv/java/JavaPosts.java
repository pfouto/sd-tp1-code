package microgram.impl.srv.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import microgram.api.Post;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import utils.Hash;

public class JavaPosts implements Posts {

	protected Map<String, Post> posts = new ConcurrentHashMap<String, Post>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	private Map<URI,Posts> postsServers = new HashMap<URI, Posts>();
	private Map<URI,Profiles> profileServers = new HashMap<URI, Profiles>();
	private Map<URI,Media> mediaServers = new HashMap<URI, Media>();

	public JavaPosts(URI[] profiles, URI[] posts, URI[] media) {

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

		try {
		Post p = this.posts.remove(postId);
		if(p == null) {
			return Result.error(Result.ErrorCode.NOT_FOUND);
		}

		this.userPosts.get(p.getOwnerId()).remove(postId);
		this.likes.remove(postId);

		this.mediaServers.values().iterator().next().delete(p.getMediaUrl().substring(p.getMediaUrl().lastIndexOf('/')+1));

		Profiles pserver = this.profileServers.values().iterator().next();

		pserver.updateNumberOfPosts(p.getOwnerId(), false);

		return Result.ok();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}

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
		Profiles pserver = this.profileServers.values().iterator().next();
		Result<Set<String>> followees = pserver.getFollowees(userId);

		if(followees.isOK()) {
			List<String> feed = new ArrayList<String>();
			for(String user: followees.value()) {
				Set<String> posts = this.userPosts.get(user);
				if(posts != null)
					feed.addAll(posts);
			}
			return Result.ok(feed);
		} else {
			return Result.error(followees.error());
		}
	}

	@Override
	public Result<Void> unlikeAllPosts(String userId) {
		boolean found = false;
		for(String post: this.likes.keySet()) {
			if(this.likes.get(post).remove(userId)) {
				found = true;
				this.posts.get(post).setLikes(this.likes.get(post).size());
			}
		}

		if(found)
			return Result.ok();

		return Result.error(Result.ErrorCode.NOT_FOUND);
	}
}
