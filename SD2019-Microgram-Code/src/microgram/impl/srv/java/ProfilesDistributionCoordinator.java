package microgram.impl.srv.java;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.RestResource;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static microgram.api.java.Result.ErrorCode.NOT_IMPLEMENTED;

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
    	System.err.println("Returning server instance " + index + " out of " + serverURLs.length);
    	System.err.println("Contacting server: " + serverURLs[index] + " :: " + instances.get(serverURLs[index]));
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

        Result<Void> res1 = getInstanceByUserId(userId1).internalFollowFront(userId1, userId2, isFollowing);
        Result<Void> res2 = getInstanceByUserId(userId2).internalFollowReverse(userId1, userId2, isFollowing);

        if(res1.isOK() && res2.isOK())
            return res1;
        else if (!res1.isOK())
            return res1;
        else
            return res2;

    }

    @Override
    public Result<Void> internalFollowFront(String userId1, String userId2, boolean isFollowing) {
        return Result.error(NOT_IMPLEMENTED);
    }

    @Override
    public Result<Void> internalFollowReverse(String userId1, String userId2, boolean isFollowing) {
        return Result.error(NOT_IMPLEMENTED);
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
