package microgram.impl.srv.rest.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {

        if (!(ex instanceof WebApplicationException) || ((WebApplicationException) ex).getResponse().getStatusInfo() != NOT_FOUND) {
            ex.printStackTrace();
        }

        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(
                MediaType.APPLICATION_JSON).build();
    }
}