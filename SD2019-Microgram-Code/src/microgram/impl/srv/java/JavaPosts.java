package microgram.impl.srv.java;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import utils.Hash;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JavaPosts implements Posts {

    private Map<String, Post> posts = new ConcurrentHashMap<>();
    private Map<String, Set<String>> likes = new ConcurrentHashMap<>();
    private Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

    private Profiles profilesClient;
    private Media mediaClient;

    public JavaPosts(URI profilesUri, URI mediaUri) {
        profilesClient = ClientFactory.getProfilesClient(profilesUri);
        mediaClient = ClientFactory.getMediaClient(mediaUri);
    }

    @Override
    public Result<Post> getPost(String postId) {
        Post res = posts.get(postId);
        return res != null ? Result.ok(res) : Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> deletePost(String postId) {
        try {
            Post p = posts.remove(postId);
            if (p == null)
                return Result.error(Result.ErrorCode.NOT_FOUND);

            userPosts.get(p.getOwnerId()).remove(postId);
            likes.remove(postId);

            mediaClient.delete(p.getMediaUrl().substring(p.getMediaUrl().lastIndexOf('/') + 1));
            profilesClient.updateNumberOfPosts(p.getOwnerId(), false);

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
            likes.put(postId, ConcurrentHashMap.newKeySet());
            Set<String> posts = userPosts.computeIfAbsent(post.getOwnerId(), k -> ConcurrentHashMap.newKeySet());
            posts.add(postId);
            profilesClient.updateNumberOfPosts(post.getOwnerId(), true);
        }
        return Result.ok(postId);
    }

    @Override
    public Result<Void> like(String postId, String userId, boolean isLiked) {

        Set<String> res = likes.get(postId);
        if (res == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if (isLiked) {
            if (!res.add(userId))
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            if (!res.remove(userId))
                return Result.error(Result.ErrorCode.NOT_FOUND);
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
            return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<List<String>> getPosts(String userId) {
        Result<Profile> profile = profilesClient.getProfile(userId);
        if (profile.isOK()) {
            Set<String> res = userPosts.get(userId);
            return Result.ok(res != null ? new ArrayList<>(res) : new ArrayList<>());
        } else {
            return Result.error(profile.error());
        }
    }

    @Override
    public Result<List<String>> getFeed(String userId) {

        Result<Set<String>> followees = profilesClient.getFollowees(userId);

        if (followees.isOK()) {
            List<String> feed = new ArrayList<>();
            for (String user : followees.value()) {
                Set<String> posts = userPosts.get(user);
                if (posts != null)
                    feed.addAll(posts);
            }
            return Result.ok(feed);
        } else {
            return Result.error(followees.error());
        }
    }

    @Override
    public Result<Void> purgeProfileActivity(String userId) {

        try {

            Set<String> postsToRemove = userPosts.remove(userId);
            if(postsToRemove != null) {
                for (String s : postsToRemove) {
                    Post p = posts.remove(s);
                    likes.remove(s);
                    mediaClient.delete(p.getMediaUrl().substring(p.getMediaUrl().lastIndexOf('/') + 1));
                }
            }

            for (String post : likes.keySet()) {
                if (likes.get(post).remove(userId)) {
                    posts.get(post).setLikes(likes.get(post).size());
                }
            }

            return Result.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}
