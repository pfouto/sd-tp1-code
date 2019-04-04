package microgram.impl.srv.rest;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import discovery.Discovery;
import microgram.impl.srv.soap.PostsSoapServer;
import microgram.impl.srv.soap.ProfilesSoapServer;
import utils.IP;


public class ProfilesRestServer {
	private static Logger Log = Logger.getLogger(ProfilesRestServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static final int PORT = 77;
	public static final String SERVICE = "Microgram-Profiles";
	public static String SERVER_BASE_URI = "http://%s:%s/rest";
	
	public static void main(String[] args) throws Exception {

		Log.setLevel( Level.FINER );

		String ip = IP.hostAddress();
		String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
		
		ResourceConfig config = new ResourceConfig();

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

		Discovery.announce(SERVICE, serverURI);
		
		URI[] profileServers = new URI[0];
		URI[] postServers = new URI[0];	
		URI[] mediaServers = new URI[0];
		
		while(profileServers.length != profiles)
				profileServers = Discovery.findUrisOf(ProfilesSoapServer.SERVICE, profiles);
		while(postServers.length != posts)
				postServers = Discovery.findUrisOf(PostsSoapServer.SERVICE, posts);
		while(mediaServers.length != 1)
			mediaServers = Discovery.findUrisOf(MediaRestServer.SERVICE, 1);
		
		config.register(new RestProfilesResources(URI.create(serverURI), profileServers, postServers, mediaServers));
		
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI.replace(ip, "0.0.0.0")), config);

		Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
		
		
	}
}
