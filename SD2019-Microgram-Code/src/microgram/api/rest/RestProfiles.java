package microgram.api.rest;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import microgram.api.Profile;
import utils.ClockedValue;

/**
 * REST API of the Profiles service.
 * 
 * Refer to the Java interface for the semantics
 * @author smd
 *
 */
@Path(RestProfiles.PATH)
public interface RestProfiles {

	String PATH="/profiles";
	
	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	Profile getProfile( @PathParam("userId") String userId );

	@DELETE
	@Path("/{userId}")
	void deleteProfile( @PathParam("userId") String userId );

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void createProfile( Profile profile );
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	void updateProfile( Profile profile);
	
	@PUT
	@Path("/{userId}/posts/{replica}")
	@Consumes(MediaType.APPLICATION_JSON)
	void updateNumberOfPosts(@PathParam("userId") String userId, @PathParam("replica") String replica, ClockedValue clockedValue);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	List<Profile> search( @QueryParam("query") String name );
	
	@PUT
	@Path("/{userId1}/following/{userId2}")
	@Consumes(MediaType.APPLICATION_JSON)
	void follow( @PathParam("userId1") String userId1, @PathParam("userId2") String userId2, boolean isFollowing);

	@PUT
	@Path("/{userId1}/internalFollowFront/{userId2}")
	@Consumes(MediaType.APPLICATION_JSON)
	void internalFollowFront( @PathParam("userId1") String userId1, @PathParam("userId2") String userId2, boolean isFollowing);

	@PUT
	@Path("/{userId1}/internalFollowReverse/{userId2}")
	@Consumes(MediaType.APPLICATION_JSON)
	void internalFollowReverse( @PathParam("userId1") String userId1,@PathParam("userId2") String userId2, boolean isFollowing);
	
	@GET
	@Path("/{userId1}/following/{userId2}")
	boolean isFollowing( @PathParam("userId1") String userId1, @PathParam("userId2") String userId2);

	@GET
	@Path("/{userId}/followees")
	Set<String> getFollowees( @PathParam("userId") String userId );
}
