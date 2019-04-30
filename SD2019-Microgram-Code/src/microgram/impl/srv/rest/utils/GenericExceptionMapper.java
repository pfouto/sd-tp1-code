package microgram.impl.srv.rest.utils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.SocketTimeoutException;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {

        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        System.err.println("Printing exception: " + ex.getMessage() + " - " + ex.getCause().getMessage());
        if (ex instanceof SocketTimeoutException || ex.getCause() instanceof SocketTimeoutException) {
            System.err.println(ex.getMessage() + " " + ex.getCause().getMessage());
        } else
            ex.printStackTrace();

        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).type(MediaType.APPLICATION_JSON).build();
    }
}