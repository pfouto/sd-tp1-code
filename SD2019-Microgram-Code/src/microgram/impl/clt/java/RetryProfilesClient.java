package microgram.impl.clt.java;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import utils.Sleep;

public class RetryProfilesClient implements Profiles {

	final Profiles impl;

	public RetryProfilesClient( Profiles impl ) {
		this.impl = impl;	
	}
	
	@Override
	public Result<Profile> getProfile(String userId) {
		while(true)
			try {
				return impl.getProfile(userId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		while(true)
			try {
				return impl.createProfile(profile);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		while(true)
			try {
				return impl.deleteProfile(userId);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		while(true)
			try {
				return impl.search(prefix);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		while(true)
			try {
				return impl.follow(userId1, userId2, isFollowing);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		while(true)
			try {
				return impl.isFollowing(userId1, userId2);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(ClientConstants.RETRY_SLEEP);
			}
	}
}
