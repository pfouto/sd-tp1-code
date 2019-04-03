package microgram.impl.clt.java;

import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import utils.Sleep;

public class RetryPostsClient implements Posts {

	final Posts impl;
	
	public RetryPostsClient( Posts impl ) {
		this.impl = impl;
	}

	@Override
	public Result<Post> getPost(String postId) {
		for (;;)
			try {
				return impl.getPost(postId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<String> createPost(Post post) {
		for (;;)
			try {
				return impl.createPost(post);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> deletePost(String postId) {
		for (;;)
			try {
				return impl.deletePost(postId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		for (;;)
			try {
				return impl.like(postId, userId, isLiked);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		for (;;)
			try {
				return impl.isLiked(postId, userId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		for (;;)
			try {
				return getPosts(userId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		for (;;)
			try {
				return impl.getFeed(userId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

}
