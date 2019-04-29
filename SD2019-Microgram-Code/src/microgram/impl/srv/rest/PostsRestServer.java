package microgram.impl.srv.rest;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import microgram.impl.srv.rest.utils.GenericExceptionMapper;
import microgram.impl.srv.rest.utils.PrematchingRequestFilter;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.impl.srv.soap.PostsSoapServer;
import microgram.impl.srv.soap.ProfilesSoapServer;
import utils.IP;


public class PostsRestServer {
	private static Logger Log = Logger.getLogger(PostsRestServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public static final int PORT = 7777;
	public static final String SERVICE = "Microgram-Posts";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";

	public static void main(String[] args) {

		Log.setLevel( Level.FINER );

		int profiles = 1;
		int posts = 1;

		for(int i = 0; i < args.length -1; i+=2)
			switch(args[i]) {
			case "-profiles": 
				profiles = Integer.parseInt(args[i+1]);
				break;
			case "-posts": 
				posts = Integer.parseInt(args[i+1]);
				break;
			}

		if(profiles <= 0 || posts <= 0) 
			throw new RuntimeException("Invalid number of servers in input.");

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

		Discovery.announce(SERVICE, serverURI);
		
		URI[] postServers = new URI[0];
		URI[] profileServers = new URI[0];
		URI[] mediaServers = new URI[0];
		
		
		while(postServers.length != posts) 
			postServers = Discovery.findUrisOf(PostsSoapServer.SERVICE, posts);
		
		while(profileServers.length != 1)
			profileServers = Discovery.findUrisOf(ProfilesSoapServer.SERVICE, 1);
		
		while(mediaServers.length != 1)
			mediaServers = Discovery.findUrisOf(MediaRestServer.SERVICE, 1);

		ResourceConfig config = new ResourceConfig();

		config.register(new RestPostsResources(ip, serverURI, postServers, profileServers[0], mediaServers[0]));
		config.register(new GenericExceptionMapper());
		config.register(new PrematchingRequestFilter());


		JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

	}
}
