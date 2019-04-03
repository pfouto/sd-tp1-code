package microgram.impl.srv.soap;

import com.sun.net.httpserver.HttpServer;
import utils.IP;

import javax.xml.ws.Endpoint;
import java.net.InetSocketAddress;
import java.util.logging.Logger;


public class ProfilesSoapServer {
	private static Logger Log = Logger.getLogger(ProfilesSoapServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s");
	}
	
	public static final int PORT = 7777;
	public static final String SERVICE = "Microgram-Profiles";
	public static String SERVER_BASE_URI = "http://%s:%s/soap";
	
	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
		Endpoint soapEndpoint = Endpoint.create(new ProfilesWebService());
		soapEndpoint.publish(server.createContext("/soap"));
		server.start();

		String ip = IP.hostAddress();
		Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE, ip + ":" + PORT));
	}
}
