package microgram.impl.srv.soap;

import com.sun.net.httpserver.HttpServer;
import discovery.Discovery;
import utils.IP;

import javax.xml.ws.Endpoint;
import java.net.InetSocketAddress;
import java.net.URI;
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

		//while(postServers.length != posts)
		URI[] postServers = Discovery.findUrisOf(PostsSoapServer.SERVICE, posts);

		Endpoint soapEndpoint = Endpoint.create(new ProfilesWebService( postServers[0]));
		soapEndpoint.publish(server.createContext("/soap"));
		server.start();


		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE, ip + ":" + PORT));
	}
}
