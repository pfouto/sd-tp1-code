package microgram.impl.clt.rest;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.api.rest.RestProfiles;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.net.URI;
import java.util.List;
import java.util.Set;

public class RestProfilesClient extends RestClient implements Profiles {

    public RestProfilesClient(URI serverUri) {
        super(serverUri, RestProfiles.PATH);
    }

    @Override
    public Result<Profile> getProfile(String userId) {
        Response r = target.path(userId)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        return responseContents(r, Status.OK, new GenericType<Profile>() {
        });
    }

    @Override
    public Result<Void> createProfile(Profile profile) {
        Response r = target.request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(profile, MediaType.APPLICATION_JSON));

        return verifyResponse(r, Status.OK);
    }

    @Override
    public Result<Void> deleteProfile(String userId) {
        Response r = target.path(userId).request().delete();
        return verifyResponse(r, Status.NO_CONTENT);
    }

    @Override
    public Result<List<Profile>> search(String prefix) {
        Response r = target.queryParam("query", prefix)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        return responseContents(r, Status.OK, new GenericType<List<Profile>>() {
        });
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
        Response r = target.path(userId1).path("following").path(userId2).request()
                .put(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));
        return verifyResponse(r, Status.OK);
    }

    @Override
    public Result<Void> internalFollowFront(String userId1, String userId2, boolean isFollowing) {
        Response r = target.path(userId1).path("internalFollowFront").path(userId2).request()
                .put(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));
        return verifyResponse(r, Status.OK);
    }

    @Override
    public Result<Void> internalFollowReverse(String userId1, String userId2, boolean isFollowing) {
        Response r = target.path(userId1).path("internalFollowReverse").path(userId2).request()
                .put(Entity.entity(isFollowing, MediaType.APPLICATION_JSON));
        return verifyResponse(r, Status.OK);
    }

    @Override
    public Result<Boolean> isFollowing(String userId1, String userId2) {
        Response r = target.path(userId1).path("following").path(userId2).request().get();
        return responseContents(r, Status.OK, new GenericType<Boolean>() {
        });
    }

	@Override
	public Result<Set<String>> getFollowees(String userId) {
		Response r = target.path(userId).path("followees").request()
				.accept(MediaType.APPLICATION_JSON).get();
		return responseContents(r, Status.OK, new GenericType<Set<String>>() {} );
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		Response r = target.request().put(Entity.entity(profile, MediaType.APPLICATION_JSON));
		
		return verifyResponse(r, Status.NO_CONTENT);
	}

	@Override
	public Result<Void> updateNumberOfPosts(String userId, boolean increase) {
		Response r = target.path(userId).path("posts")
				.request().put(Entity.entity(increase, MediaType.APPLICATION_JSON));
		
		return verifyResponse(r, Status.NO_CONTENT);
	}
}