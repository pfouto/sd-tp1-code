package microgram.impl.clt.java;

import microgram.api.java.Media;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.impl.clt.rest.RestMediaClient;
import microgram.impl.clt.rest.RestPostsClient;
import microgram.impl.clt.rest.RestProfilesClient;
import microgram.impl.clt.soap.SoapPostsClient;
import microgram.impl.clt.soap.SoapProfilesClient;

import java.net.URI;

public class ClientFactory {

    private static final String REST = "/rest";
    private static final String SOAP = "/soap";

    public static Media getMediaClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryMediaClient(new RestMediaClient(uri));
        else if (uriString.endsWith(SOAP))
            throw new RuntimeException(
                    "Non Useful Work Detected: We did not want for you to build a Media SOAP Server: " + uri);

        throw new RuntimeException("Unknown service type..." + uri);
    }

    public static Posts getPostsClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryPostsClient(new RestPostsClient(uri));
        else if (uriString.endsWith(SOAP))
            return new RetryPostsClient(new SoapPostsClient(uri));

        throw new RuntimeException("Unknown service type..." + uri);
    }

    public static Profiles getProfilesClient(URI uri) {
        String uriString = uri.toString();
        if (uriString.endsWith(REST))
            return new RetryProfilesClient(new RestProfilesClient(uri));
        else if (uriString.endsWith(SOAP))
            return new RetryProfilesClient(new SoapProfilesClient(uri));

        throw new RuntimeException("Unknown service type..." + uri);
    }
}
