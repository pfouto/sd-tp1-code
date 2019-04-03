package microgram.impl.srv.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.api.java.Result.ErrorCode;
import utils.Hash;

public class JavaPosts implements Posts {

	protected Map<String, Post> posts = new HashMap<>();
	protected Map<String, Set<String>> likes = new HashMap<>();
	protected Map<String, Set<String>> userPosts = new HashMap<>();
	
	private int nProfiles;
	private int nPosts;
	
	public JavaPosts() {
		this(1,1);
	}
	
	public JavaPosts(int nprofiles, int nposts) {
		this.nProfiles = nprofiles;
		this.nPosts = nposts;
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
		return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
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
