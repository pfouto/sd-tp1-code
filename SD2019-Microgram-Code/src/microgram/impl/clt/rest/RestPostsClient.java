package microgram.impl.clt.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.api.rest.RestPosts;

public class RestPostsClient extends RestClient implements Posts {

	public RestPostsClient(URI serverUri) {
		super(serverUri, RestPosts.PATH);
	}

	@Override
	public Result<Post> getPost(String postId) {
		Response r = target.path(postId)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		return responseContents(r, Status.OK, new GenericType<Post>() {
		});
	}

	@Override
	public Result<String> createPost(Post post) {
		Response r = target
				.request()
				.post( Entity.entity( post, MediaType.APPLICATION_JSON));
		return super.responseContents(r, Status.OK, new GenericType<String>(){});	
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Response r = target.path(postId).request().delete();
		return verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		Response r = target.path(postId).path("likes").path(userId).request()
				.put(Entity.entity(isLiked, MediaType.APPLICATION_JSON));
		return verifyResponse(r, Status.OK);
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Response r = target.path(postId).path("likes").path(userId).request().get();
		return responseContents(r, Status.OK, new GenericType<Boolean>() {
		});
	}
	@Override
	public Result<List<String>> getPosts(String userId) {
		Response r = target.path("from").path(userId).request().get();
		return responseContents(r, Status.OK, new GenericType<List<String>>() {
		});
	}

	@Override
	public Result<List<String>> getPostsInternal(String userId) {
		Response r = target.path("fromInternal").path(userId).request().get();
		return responseContents(r, Status.OK, new GenericType<List<String>>() {
		});
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		Response r = target.path("feed").path(userId).request().get();
		return responseContents(r, Status.OK, new GenericType<List<String>>() {
		});
	}

	@Override
	public Result<Void> purgeProfileActivity(String userId) {
		Response r = target.path("purge").path(userId).request().delete();
		return verifyResponse(r, Status.NO_CONTENT);
	}
}
