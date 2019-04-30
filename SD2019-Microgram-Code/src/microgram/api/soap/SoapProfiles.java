package microgram.api.soap;

import microgram.api.Profile;
import utils.ClockedValue;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.List;
import java.util.Set;

@WebService(serviceName=SoapProfiles.NAME, targetNamespace=SoapProfiles.NAMESPACE, endpointInterface=SoapProfiles.INTERFACE)
public interface SoapProfiles {
	
	String NAME = "profiles";
	String NAMESPACE = "http://sd2019";
	String INTERFACE = "microgram.api.soap.SoapProfiles";
	
	@WebMethod
	Profile getProfile( String userId ) throws MicrogramException;
		
	@WebMethod
	void createProfile( Profile profile ) throws MicrogramException;

	@WebMethod
	void updateProfile( Profile profile) throws MicrogramException;
	
	@WebMethod
	void deleteProfile( String userId ) throws MicrogramException;

	@WebMethod
	void updateNumberOfPosts(String userId, String replica, ClockedValue clockedValue) throws MicrogramException;

	@WebMethod
	List<Profile> search( String prefix ) throws MicrogramException;
	
	@WebMethod
	void follow( String userId1, String userId2, boolean isFollowing) throws MicrogramException;

	@WebMethod
	void internalFollowFront( String userId1, String userId2, boolean isFollowing) throws MicrogramException;

	@WebMethod
	void internalFollowReverse( String userId1, String userId2, boolean isFollowing) throws MicrogramException;

	@WebMethod
	boolean isFollowing( String userId1, String userId2) throws MicrogramException;	
	
	@WebMethod
	Set<String> getFollowees( String userId ) throws MicrogramException;
}
