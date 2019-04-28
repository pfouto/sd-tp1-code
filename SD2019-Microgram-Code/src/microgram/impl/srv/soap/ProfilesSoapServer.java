package microgram.impl.srv.soap;

import com.sun.net.httpserver.HttpServer;
import discovery.Discovery;
import utils.IP;

import javax.xml.ws.Endpoint;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


public class ProfilesSoapServer {
	private static Logger Log = Logger.getLogger(ProfilesSoapServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public static final int PORT = 3333;
	public static final String SERVICE = "Microgram-Profiles";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	public static void main(String[] args) throws Exception {
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

		HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
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

		URI[] profileServers = new URI[0], postsServers = new URI[0];
		while(profileServers.length != profiles)
			profileServers = Discovery.findUrisOf(ProfilesSoapServer.SERVICE, profiles);
		while(postsServers.length != 1)
			postsServers = Discovery.findUrisOf(PostsSoapServer.SERVICE, 1);

		Endpoint soapEndpoint = Endpoint.create(new ProfilesWebService(serverURI, profileServers, postsServers[0] ));

		server.setExecutor(Executors.newCachedThreadPool());
		soapEndpoint.publish(server.createContext("/soap"));

		server.start();

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE, ip + ":" + PORT));
	}
}
