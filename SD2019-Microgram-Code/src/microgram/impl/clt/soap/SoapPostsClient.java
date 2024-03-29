package microgram.impl.clt.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import com.sun.xml.ws.developer.JAXWSProperties;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapPosts;
import microgram.impl.clt.java.RetryClient;

public class SoapPostsClient extends SoapClient implements Posts {

	private QName qname = new QName(SoapPosts.NAMESPACE, SoapPosts.NAME);

	SoapPosts impl;	

	public SoapPostsClient(URI serverUri) {
		super(serverUri);
	}

	private SoapPosts impl() {
		if( impl == null ) {
			Service service = Service.create(super.wsdl(), qname);
			impl = service.getPort(SoapPosts.class);
			((BindingProvider) impl).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, RetryClient.READ_TIMEOUT);
			((BindingProvider) impl).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, RetryClient.CONNECT_TIMEOUT);
		}
		return impl;
	}

	@Override
	public Result<Post> getPost(String postId) {
		try {
			return Result.ok(impl().getPost(postId));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}


	@Override
	public Result<String> createPost(Post post) {
		try {
			return Result.ok(impl().createPost(post));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<Void> deletePost(String postId) {
		try {
			impl().deletePost(postId);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		try {
			impl().like(postId, userId, isLiked);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		try {
			return Result.ok(impl().isLiked(postId, userId));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		try {
			return Result.ok(impl().getPosts(userId));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<List<String>> getPostsInternal(String userId) {
		try {
			return Result.ok(impl().getPostsInternal(userId));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		try {
			return Result.ok(impl().getFeed(userId));
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}

	@Override
	public Result<Void> purgeProfileActivity(String userId) {
		try {
			impl().purgeProfileActivity(userId);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(errorCode(e));
		}
	}
}
