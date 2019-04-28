package microgram.api;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a user Profile
 * 
 * A user Profile has an unique userId; a comprises: the user's full name; and, a photo, stored at some photourl. This information is immutable.
 * The profile also gathers the user's statistics: ie., the number of posts made, the number of profiles the user is following, the number of profiles following this user. 
 * All these are mutable.
 * 
 * @author smd
 *
 */
public class Profile {
	
	String userId;
	String fullName;
	String photoUrl;
	
	AtomicInteger posts;
	int following;
	int followers;

	public Profile() {
		this.posts = new AtomicInteger(0);
	}

	public Profile(String userId, String fullName, String photoUrl, int posts, int following, int followers) {
		this.userId = userId;
		this.fullName = fullName;
		this.photoUrl = photoUrl;
		this.posts = new AtomicInteger(posts);
		this.following = following;
		this.followers = followers;
	}

	@Override
	public String toString() {
		return "Profile{" +
				"userId='" + userId + '\'' +
				", fullName='" + fullName + '\'' +
				", photoUrl='" + photoUrl + '\'' +
				", posts=" + posts +
				", following=" + following +
				", followers=" + followers +
				'}';
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public int getPosts() {
		return posts.get();
	}

	public void setPosts(int posts) {
		this.posts = new AtomicInteger(posts);
	}

	public void incPosts(boolean inc) {
		if (inc) posts.incrementAndGet();
		else posts.decrementAndGet();
	}

	public int getFollowing() {
		return following;
	}

	public void setFollowing(int following) {
		this.following = following;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}
}
