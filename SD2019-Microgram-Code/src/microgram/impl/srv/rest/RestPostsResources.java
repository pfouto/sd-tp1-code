package microgram.impl.srv.rest;

import java.net.URI;
import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.rest.RestPosts;
import microgram.impl.srv.java.JavaPosts;
import microgram.impl.srv.java.PostsDistributionCoordinator;

public class RestPostsResources extends RestResource implements RestPosts {

	final Posts impl;
		
	public RestPostsResources(String myIp, String serverURI, URI[] postsURIs, URI profilesUri, URI mediaUri) {
		this.impl = new PostsDistributionCoordinator(myIp, serverURI, postsURIs, profilesUri, mediaUri);
	}
	
	@Override
	public Post getPost(String postId) {
		return super.resultOrThrow(impl.getPost(postId));
	}

	@Override
	public void deletePost(String postId) {
		super.resultOrThrow(impl.deletePost(postId));
	}

	@Override
	public String createPost(Post post) {
		return super.resultOrThrow(impl.createPost(post));
	}

	@Override
	public boolean isLiked(String postId, String userId) {
		return super.resultOrThrow(impl.isLiked(postId, userId));
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) {
		super.resultOrThrow(impl.like(postId, userId, isLiked));
	}

	@Override
	public List<String> getPosts(String userId) {
		return super.resultOrThrow(impl.getPosts(userId));
	}

	@Override
	public List<String> getPostsInternal(String userId) {
		return super.resultOrThrow(impl.getPostsInternal(userId));
	}

	@Override
	public List<String> getFeed(String userId) {
		return super.resultOrThrow(impl.getFeed(userId));
	}

	@Override
	public void purgeProfileActivity(String userId) {
		super.resultOrThrow(impl.purgeProfileActivity(userId));
		
	}
 
}
