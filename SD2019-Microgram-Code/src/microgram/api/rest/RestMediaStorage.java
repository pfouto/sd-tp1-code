package microgram.api.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * REST API of the media storage service...
 * 
 * @author smd
 *
 */
@Path(RestMediaStorage.PATH)
public interface RestMediaStorage {
	
	String PATH="/media";
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	String upload( byte[] bytes);
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	byte[] download(@PathParam("id") String id);

	@DELETE
	@Path("/{id}")
	void delete(@PathParam("id") String id);
}

