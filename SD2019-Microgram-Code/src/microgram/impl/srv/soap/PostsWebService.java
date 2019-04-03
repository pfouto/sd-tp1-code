package microgram.impl.srv.soap;

import java.util.List;

import javax.jws.WebService;

import microgram.api.Post;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapPosts;

@WebService(serviceName=SoapPosts.NAME, targetNamespace=SoapPosts.NAMESPACE, endpointInterface=SoapPosts.INTERFACE)
public class PostsWebService extends SoapService implements SoapPosts {

	@Override
	public Post getPost(String postId) throws MicrogramException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deletePost(String postId) throws MicrogramException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String createPost(Post post) throws MicrogramException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLiked(String postId, String userId) throws MicrogramException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void like(String postId, String userId, boolean isLiked) throws MicrogramException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getPosts(String userId) throws MicrogramException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getFeed(String userId) throws MicrogramException {
		// TODO Auto-generated method stub
		return null;
	}


}
