package microgram.impl.srv.soap;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpServer;

import discovery.Discovery;
import microgram.impl.srv.rest.MediaRestServer;
import utils.IP;


public class PostsSoapServer {
	private static Logger Log = Logger.getLogger(PostsSoapServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}

	public static final int PORT = 2222;
	public static final String SERVICE = "Microgram-Posts";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";

	public static void main(String[] args) throws Exception {
		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
						   "true");

		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump",
						   "true");

		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");

		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump",
						   "true");

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
		Discovery.announce(SERVICE, String.format(SERVER_BASE_URI, ip, PORT));

		//while(profileServers.length != profiles)
		URI[] profileServers = Discovery.findUrisOf(ProfilesSoapServer.SERVICE, profiles);
		//while(mediaServers.length != 1)
		URI[] mediaServers = Discovery.findUrisOf(MediaRestServer.SERVICE, 1);

		Endpoint soapEndpoint = Endpoint.create(new PostsWebService(profileServers[0], mediaServers[0]));

		server.setExecutor(Executors.newCachedThreadPool());
		soapEndpoint.publish(server.createContext("/soap"));

		server.start();

		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE, ip + ":" + PORT));
	}
}
