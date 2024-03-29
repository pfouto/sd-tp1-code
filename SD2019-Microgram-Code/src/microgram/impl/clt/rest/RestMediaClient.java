package microgram.impl.clt.rest;

import microgram.api.java.Media;
import microgram.api.java.Result;
import microgram.api.rest.RestMediaStorage;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

public class RestMediaClient extends RestClient implements Media {

    public RestMediaClient(URI mediaStorage) {
        super(mediaStorage, RestMediaStorage.PATH);
    }

    @Override
    public Result<String> upload(byte[] bytes) {
        Response r = target.request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));

        return responseContents(r, Status.OK, new GenericType<String>() {
        });
    }

    @Override
    public Result<byte[]> download(String id) {
        Response r = target.path(id)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        return responseContents(r, Status.OK, new GenericType<byte[]>() {
        });
    }

    @Override
    public Result<Void> delete(String id) {
        Response r = target.path(id).request().delete();
        return verifyResponse(r, Status.NO_CONTENT);
    }
}