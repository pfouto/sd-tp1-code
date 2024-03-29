package microgram.impl.srv.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import microgram.api.java.Result;

public class RestResource {

	/**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception matching the error code...
	 * @param result
	 * @return
	 */
	protected <T> T resultOrThrow( Result<T> result ) {
		if( result.isOK() )
			return result.value();
		else
			throw new WebApplicationException( statusCode(result));
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	static private Status statusCode( Result<?> result ) {
		switch( result.error() ) {
			case NOT_FOUND: 
				return Status.NOT_FOUND;
			case CONFLICT:
				return Status.CONFLICT;
			case OK:
				return result.value() == null ? Status.NO_CONTENT: Status.OK;
			case NOT_IMPLEMENTED:
				return Status.NOT_IMPLEMENTED;
			case INTERNAL_ERROR:
			default:
				return Status.INTERNAL_SERVER_ERROR;
		}
	}
}
