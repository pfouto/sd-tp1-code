package microgram.impl.clt.rest;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.glassfish.jersey.client.ClientConfig;

import microgram.api.java.Result;
import microgram.api.java.Result.ErrorCode;
import microgram.impl.clt.java.ClientConstants;

import org.glassfish.jersey.client.ClientProperties;

abstract class RestClient {

	protected final URI uri;
    protected final Client client;
    protected final WebTarget target;
    protected final ClientConfig config;

    public RestClient(URI uri, String path) {
        this.uri = uri;
        this.config = new ClientConfig();
        config.property(ClientProperties.CONNECT_TIMEOUT, ClientConstants.CONNECT_TIMEOUT);
        config.property(ClientProperties.READ_TIMEOUT, ClientConstants.READ_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
        this.target = this.client.target(uri).path(path);
    }

    // Get the actual response, when the status matches what was expected, otherwise
    // return a default value
    protected <T> Result<T> verifyResponse(Response r, Status expected) {
        try {
            StatusType status = r.getStatusInfo();
            if (status.equals(expected))
                return ok();
            else
                return error(errorCode(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    // Get the actual response, when the status matches what was expected, otherwise
    // return a default value
    protected <T> Result<T> responseContents(Response r, Status expected, GenericType<T> gtype) {
        try {
            StatusType status = r.getStatusInfo();
            if (status.equals(expected))
                return ok(r.readEntity(gtype));
            else
                return error(errorCode(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    static private ErrorCode errorCode(int status) {
        switch (status) {
            case 200:
            case 209:
                return ErrorCode.OK;
            case 404:
                return ErrorCode.NOT_FOUND;
            case 409:
                return ErrorCode.CONFLICT;
            case 501:
                return ErrorCode.NOT_IMPLEMENTED;
            case 500:
            default:
                return ErrorCode.INTERNAL_ERROR;
        }
    }
}
