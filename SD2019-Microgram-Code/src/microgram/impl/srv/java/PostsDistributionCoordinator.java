package microgram.impl.srv.java;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import utils.Hash;

import java.net.URI;
import java.util.*;

public class PostsDistributionCoordinator implements Posts {

    private Profiles profilesClient;
    private SortedMap<String, Posts> instances;
    private String[] serverURLs;

    private String myServerURI;
    
    public PostsDistributionCoordinator(String myIp, String myServerURI, URI[] postsURIs, URI profilesUri, URI mediaUri) {
        this.myServerURI = myServerURI;

        profilesClient = ClientFactory.getProfilesClient(profilesUri);
        ClientFactory.getMediaClient(mediaUri);
        instances = new TreeMap<>();
        
        for(URI u: postsURIs) {
        	if ( u.toString().equalsIgnoreCase(myServerURI) ) {
        		instances.put(u.toString(), new JavaPosts(myIp, profilesUri, mediaUri));
        	} else {
        		instances.put(u.toString(), ClientFactory.getPostsClient(u));
        	}
        }
        serverURLs = instances.keySet().toArray(new String[instances.keySet().size()]);
    }

    private Posts getInstanceByPostId(String postId){
    	int index = ((int) Character.toLowerCase(postId.charAt(0))) % serverURLs.length;
        System.out.println("Returning instance " + index + "/" + serverURLs.length + " - " + serverURLs[index] + "::" + instances.get(serverURLs[index]) + " for id " + postId);
        return instances.get(serverURLs[index]);
    }
    
    @Override
    public Result<Post> getPost(String postId) {
        return this.getInstanceByPostId(postId).getPost(postId);
    }

    @Override
    public Result<Void> deletePost(String postId) {
    	return this.getInstanceByPostId(postId).deletePost(postId);
    }

    @Override
    public Result<String> createPost(Post post) {
        String postId = Hash.of(post.getOwnerId(), post.getMediaUrl());
        return this.getInstanceByPostId(postId).createPost(post);
    }

    @Override
    public Result<Void> like(String postId, String userId, boolean isLiked) {
    	return this.getInstanceByPostId(postId).like(postId, userId, isLiked);
    }

    @Override
    public Result<Boolean> isLiked(String postId, String userId) {
    	return this.getInstanceByPostId(postId).isLiked(postId, userId);
    }

    @Override
    public Result<List<String>> getPosts(String userId) {

       Result<Profile> profile = profilesClient.getProfile(userId);
        if (!profile.isOK())
            return Result.error(profile.error());

        List<String> posts = new ArrayList<>();
        for(Posts p: this.instances.values()) {
            Result<List<String>> r = p.getPostsInternal(userId);
            if(r.isOK())
                posts.addAll(r.value());
        }
        return Result.ok(posts);
    }

    @Override
    public Result<List<String>> getPostsInternal(String userId) {
        Posts posts = instances.get(myServerURI);
        if(!(posts instanceof JavaPosts))
            throw new AssertionError("getPostsInternal trying to access remote replica...");
        return posts.getPostsInternal(userId);
    }

    @Override
    public Result<List<String>> getFeed(String userId) {

        Result<Set<String>> followees = profilesClient.getFollowees(userId);
        if (!followees.isOK())
            return Result.error(followees.error());

        List<String> feed = new ArrayList<>();
        for (String user : followees.value()) {
            Result<List<String>> posts = this.getPosts(user);
            if(posts.isOK())
                feed.addAll(posts.value());
        }
        return Result.ok(feed);
    }

    @Override
    public Result<Void> purgeProfileActivity(String userId) {
    	for(Posts p: this.instances.values()) {
    		p.purgeProfileActivity(userId);
    	}
    	
    	return Result.ok();
    }
}
