package microgram.impl.srv.java;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.java.ClientFactory;
import microgram.impl.srv.rest.RestResource;
import utils.ClockedValue;

import java.net.URI;
import java.util.*;

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
        serverURLs = instances.keySet().toArray(new String[0]);
        System.err.println("Map: " + instances);
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
        try {
            Profiles instance = getInstanceByUserId(profile.getUserId());
            return instance.createProfile(profile);
        } catch (Exception e){
            e.printStackTrace();
            return Result.error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteProfile(String userId) {
        return getInstanceByUserId(userId).deleteProfile(userId);
    }

    @Override
    public Result<List<Profile>> search(String prefix) {
    	if(prefix.equalsIgnoreCase("")) {
    		List<Profile> list = new ArrayList<>();
    		for(Profiles p: this.instances.values()) {
    			Result<List<Profile>> partial = p.search(prefix);
    			if(partial.isOK()) {
    				list.addAll(partial.value());
    			}
    		}
    		return Result.ok(list);
    	} else
    		return getInstanceByUserId(prefix).search(prefix);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {

        //Check if both users exist before calling internal follow methods...
        //Alternative would be to check existence of other user inside "internalFollowFront" and "internalFollowReverse"
        Result<Profile> res1 = getInstanceByUserId(userId1).getProfile(userId1);
        Result<Profile> res2 = getInstanceByUserId(userId2).getProfile(userId2);

        if (!res1.isOK() || !res2.isOK()) {
            if (!res1.isOK())
                return Result.error(res1.error());
            else
                return Result.error(res2.error());
        }


        Result<Void> res3 = getInstanceByUserId(userId1).internalFollowFront(userId1, userId2, isFollowing);
        Result<Void> res4 = getInstanceByUserId(userId2).internalFollowReverse(userId1, userId2, isFollowing);

        if(res3.isOK() && res4.isOK())
            return res3;
        else if (!res3.isOK())
            return res3;
        else
            return res4;
    }

    @Override
    public Result<Void> internalFollowFront(String userId1, String userId2, boolean isFollowing) {
        Profiles instance = getInstanceByUserId(userId1);
        //Make sure internal call was forwarded to the correct replica...
        if(!(instance instanceof JavaProfiles))
            throw new AssertionError("internalFollowFront received in wrong replica");
        return instance.internalFollowFront(userId1, userId2, isFollowing);
    }

    @Override
    public Result<Void> internalFollowReverse(String userId1, String userId2, boolean isFollowing) {
        Profiles instance = getInstanceByUserId(userId2);
        //Make sure internal call was forwarded to the correct replica...
        if(!(instance instanceof JavaProfiles))
            throw new AssertionError("internalFollowReverse received in wrong replica");
        return instance.internalFollowReverse(userId1, userId2, isFollowing);

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
    public Result<Void> updateNumberOfPosts(String userId, String replica, ClockedValue clockedValue) {
        return getInstanceByUserId(userId).updateNumberOfPosts(userId, replica, clockedValue);
    }

}
