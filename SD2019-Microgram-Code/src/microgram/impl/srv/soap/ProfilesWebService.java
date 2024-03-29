package microgram.impl.srv.soap;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.jws.WebService;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapProfiles;
import microgram.impl.srv.java.ProfilesDistributionCoordinator;
import utils.ClockedValue;

@WebService(serviceName=SoapProfiles.NAME, targetNamespace=SoapProfiles.NAMESPACE, endpointInterface=SoapProfiles.INTERFACE)
public class ProfilesWebService extends SoapService implements SoapProfiles {

	final Profiles impl;
	
	public ProfilesWebService(String serverURI, URI[] profilesURIs, URI postsUri) {
		this.impl = new ProfilesDistributionCoordinator(serverURI, profilesURIs, postsUri);
	}
	
	@Override
	public Profile getProfile( String userId ) throws MicrogramException {
		return super.resultOrThrow( impl.getProfile(userId));
	}

	@Override
	public void createProfile(Profile profile) throws MicrogramException {
		super.resultOrThrow( impl.createProfile(profile));
	}

	@Override
	public void deleteProfile(String userId) throws MicrogramException {
		super.resultOrThrow( impl.deleteProfile(userId));
	}

	@Override
	public List<Profile> search(String prefix) throws MicrogramException {
		return super.resultOrThrow( impl.search(prefix));
	}

	@Override
	public void follow(String userId1, String userId2, boolean isFollowing) throws MicrogramException {
		super.resultOrThrow( impl.follow(userId1, userId2, isFollowing));
	}

	@Override
	public void internalFollowFront(String userId1, String userId2, boolean isFollowing) throws MicrogramException {
		super.resultOrThrow( impl.internalFollowFront(userId1, userId2, isFollowing));
	}

	@Override
	public void internalFollowReverse(String userId1, String userId2, boolean isFollowing) throws MicrogramException {
		super.resultOrThrow( impl.internalFollowReverse(userId1, userId2, isFollowing));
	}

	@Override
	public boolean isFollowing(String userId1, String userId2) throws MicrogramException {
		return super.resultOrThrow( impl.isFollowing(userId1, userId2));
	}

	@Override
	public Set<String> getFollowees(String userId) throws MicrogramException {
		return super.resultOrThrow( impl.getFollowees(userId));
	}

	@Override
	public void updateProfile(Profile profile) throws MicrogramException {
		super.resultOrThrow( impl.updateProfile(profile) );
	}

	@Override
	public void updateNumberOfPosts(String userId, String replica, ClockedValue clockedValue) throws MicrogramException {
		super.resultOrThrow( impl.updateNumberOfPosts(userId, replica, clockedValue) );
	}
	
}
