package microgram.impl.clt.java;

import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.impl.clt.rest.RestMediaClient;
import microgram.impl.clt.rest.RestPostsClient;
import microgram.impl.clt.rest.RestProfilesClient;

import java.net.URI;

public class ClientFactory {

    private static final String REST = "/rest";

    public static Media getMediaClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryMediaClient(new RestMediaClient(uri));

        throw new RuntimeException("Unknown service type..." + uri);
    }

    public static Posts getPostsClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryPostsClient(new RestPostsClient(uri));

        throw new RuntimeException("Unknown service type..." + uri);
    }

    public static Profiles getProfilesClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryProfilesClient(new RestProfilesClient(uri));

        throw new RuntimeException("Unknown service type..." + uri);
    }
}
