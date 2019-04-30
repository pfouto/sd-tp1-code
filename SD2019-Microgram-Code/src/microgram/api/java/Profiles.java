package microgram.api.java;

import java.util.List;
import java.util.Set;

import microgram.api.Profile;
import utils.ClockedValue;

/**
 * 
 * Interface for the service that manages Users/Profiles.
 * 
 */
public interface Profiles {

	/**
	 * Obtains a profile
	 * @param userId unique identifier of the requested profile
	 * @return result of (OK,Profile), or NOT_FOUND
	 */
	Result<Profile> getProfile( String userId );
	
	/**
	 * Creates a profile 
	 * @param profile to be created
	 * @return result of (OK,), or CONFLICT
	 */
	Result<Void> createProfile( Profile profile );
		
	/**
	 * Updates an existing profile
	 * @param profile to be update
	 * @return result of (OK,), or NOT_FOUND
	 */
	Result<Void> updateProfile( Profile profile );
	
	/**
	 * Updates the number of posts of a user profile
	 * @param userId is of profile to be manipulated
	 * @param replica is the replica that is updating the number
	 * @param clockedValue is the new number of posts that replica holds, and its clock (to fix unordered deliveries)
	 * @return result of (OK,), or NOT_FOUND or CONFLICT
	 */
	Result<Void> updateNumberOfPosts(String userId, String replica, ClockedValue clockedValue);

	
	/**
	 * Delete a profile 
	 * @param userId identifier of the profile to be deleted
	 * @return result of (OK,), or NOT_FOUND
	 */
	Result<Void> deleteProfile( String userId );
	
	/**
	 * Searches for profiles by prefix of the profile identifier
	 * @param prefix - the prefix used to match identifiers
	 * @return result of (OK, List<Profile>); an empty list if the search yields no profiles
	 */
	Result<List<Profile>> search( String prefix );
	
	/**
	 * Causes a profile to follow or stop following another.
	 * 
	 * @param userId1 the profile that will follow or cease to follow the followed profile
	 * @param userId2 the followed profile 
	 * @param isFollowing flag that indicates the desired end status of the operation
	 * @return
	 */
	Result<Void> follow( String userId1, String userId2, boolean isFollowing);

	//Internal methods since follow logic is now split between two instances of the service.
	Result<Void> internalFollowFront( String userId1, String userId2, boolean isFollowing);
	Result<Void> internalFollowReverse( String userId1, String userId2, boolean isFollowing);
	
	/**
	 * Checks if a profile is following another or not
	 * 
	 * @param userId1 the follower profile
	 * @param userId2 the followed profile
	 * @return (OK,Boolean), NOT_FOUND if any of the profiles does not exist
	 */
	Result<Boolean> isFollowing( String userId1, String userId2);
	
	/**
	 * Obtains the followees of a user
	 * 
	 * @param userId the follow profile
	 * @return (OK,Set<String>) containing a set of user identifiers or NOT_FOUND if the profile identified by userId does not exists
	 */
	
	Result<Set<String>> getFollowees( String userId );
}
