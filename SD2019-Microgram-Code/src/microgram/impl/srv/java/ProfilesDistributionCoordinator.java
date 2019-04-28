package microgram.impl.srv.java;

import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.RestResource;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;

public class ProfilesDistributionCoordinator extends RestResource implements Profiles {

    private SortedMap<String, Profiles> instances;
    private String[] serverURLs;

    public ProfilesDistributionCoordinator(String myServerURI, URI[] profilesURIs, URI postsURI) {
        instances = new TreeMap<>();
        for(URI u: profilesURIs) {
        	if ( u.toString().equalsIgnoreCase(myServerURI) ) {
        		instances.put(u.toString(), new JavaProfiles(ClientFactory.getPostsClient(postsURI)));
        	} else {
        		instances.put(u.toString(), ClientFactory.getProfilesClient(u));
        	}
        }
        serverURLs = instances.keySet().toArray(new String[instances.keySet().size()]);
    }

    private Profiles getInstanceByUserId(String userId){
    	int index = ((int) Character.toLowerCase(userId.charAt(0))) % serverURLs.length;
    	return instances.get(serverURLs[index]);
    }

    @Override
    public Result<Profile> getProfile(String userId) {
        return getInstanceByUserId(userId).getProfile(userId);
    }

    @Override
    public Result<Void> createProfile(Profile profile) {
        return getInstanceByUserId(profile.getUserId()).createProfile(profile);
    }

    @Override
    public Result<Void> deleteProfile(String userId) {
        return getInstanceByUserId(userId).deleteProfile(userId);
    }

    @Override
    public Result<List<Profile>> search(String prefix) {
        return getInstanceByUserId(prefix).search(prefix);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {

        getInstanceByUserId(userId1).follow(userId1, userId2, isFollowing);
        getInstanceByUserId(userId2).follow(userId1, userId2, isFollowing);

        Set<String> s1 = following.get(userId1);
        Set<String> s2 = followers.get(userId2);

        if (s1 == null || s2 == null)
            return Result.error(Result.ErrorCode.NOT_FOUND);

        if (isFollowing) {
            boolean added1 = s1.add(userId2), added2 = s2.add(userId1);
            if (!added1 || !added2)
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            boolean removed1 = s1.remove(userId2), removed2 = s2.remove(userId1);
            if (!removed1 || !removed2)
                return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Boolean> isFollowing(String userId1, String userId2) {
        return getInstanceByUserId(userId1).isFollowing(userId1, userId2);
    }

    @Override
    public Result<Set<String>> getFollowees(String userId) {
        return getInstanceByUserId(userId).getFollowees(userId);
    }

    @Override
    public Result<Void> updateProfile(Profile profile) {
        return getInstanceByUserId(profile.getUserId()).updateProfile(profile);
    }

    @Override
    public Result<Void> updateNumberOfPosts(String userId, boolean increase) {
        return getInstanceByUserId(userId).updateNumberOfPosts(userId, increase);
    }

}
