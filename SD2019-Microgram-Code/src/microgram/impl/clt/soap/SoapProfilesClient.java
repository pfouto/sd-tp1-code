package microgram.impl.clt.soap;

import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import com.sun.xml.ws.developer.JAXWSProperties;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapProfiles;
import microgram.impl.clt.java.ClientConstants;

public class SoapProfilesClient extends SoapClient implements Profiles {

	private QName qname = new QName(SoapProfiles.NAMESPACE, SoapProfiles.NAME);

	SoapProfiles impl;	
	
	public SoapProfilesClient(URI serverUri) {
		super(serverUri);
	}

	private SoapProfiles impl() {
		if( impl == null ) {
			Service service = Service.create(super.wsdl(), qname);
			impl = service.getPort(SoapProfiles.class);
			((BindingProvider) impl).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, ClientConstants.READ_TIMEOUT);
			((BindingProvider) impl).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, ClientConstants.READ_TIMEOUT);
		}
		return impl;
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		try {
			return Result.ok(impl().getProfile(userId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		try {
			impl().createProfile(profile);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		try {
			impl().deleteProfile(userId);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		try {
			return Result.ok(impl().search(prefix));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		try {
			impl().follow(userId1, userId2, isFollowing);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		try {
			return Result.ok(impl().isFollowing(userId1, userId2));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Set<String>> getFollowees(String userId) {
		try {
			return Result.ok(impl().getFollowees(userId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> updateProfile(Profile profile) {
		try {
			impl().updateProfile(profile);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> updateNumberOfPosts(String userId, boolean increase) {
		try {
			impl().updateNumberOfPosts(userId, increase);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

}
