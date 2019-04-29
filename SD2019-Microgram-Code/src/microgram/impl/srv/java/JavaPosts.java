package microgram.impl.srv.java;

import microgram.api.Post;
import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import utils.Hash;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JavaPosts implements Posts {

    private Map<String, Post> posts = new ConcurrentHashMap<>();
    private Map<String, Set<String>> likes = new ConcurrentHashMap<>();
    private Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

    private Profiles profilesClient;
    private Media mediaClient;

    private String myIp;

    JavaPosts(String myIp, URI profilesUri, URI mediaUri) {
        this.myIp = myIp;
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

            likes.remove(postId);
            mediaClient.delete(p.getMediaUrl().substring(p.getMediaUrl().lastIndexOf('/') + 1));

            Set<String> singleUserPosts = userPosts.get(p.getOwnerId());
            singleUserPosts.remove(postId);
            synchronized (singleUserPosts) {
                profilesClient.updateNumberOfPosts(p.getOwnerId(), myIp, singleUserPosts.size());
            }

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

            Set<String> singleUserPosts = userPosts.computeIfAbsent(post.getOwnerId(), k -> ConcurrentHashMap.newKeySet());
            singleUserPosts.add(postId);
            synchronized (singleUserPosts) {
                profilesClient.updateNumberOfPosts(post.getOwnerId(), myIp, singleUserPosts.size());
            }

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
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<List<String>> getPostsInternal(String userId) {
        Set<String> res = userPosts.get(userId);
        return Result.ok(res != null ? new ArrayList<>(res) : new ArrayList<>());
    }

    @Override
    public Result<List<String>> getFeed(String userId) {
        return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> purgeProfileActivity(String userId) {

        try {

            Set<String> postsToRemove = userPosts.remove(userId);
            if (postsToRemove != null) {
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
