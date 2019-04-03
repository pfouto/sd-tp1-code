package microgram.impl.clt.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import com.sun.xml.internal.ws.client.BindingProviderProperties;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.api.soap.MicrogramException;
import microgram.api.soap.SoapPosts;
import microgram.impl.clt.java.ClientConstants;

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
			((BindingProvider) impl).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, ClientConstants.READ_TIMEOUT);
			((BindingProvider) impl).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, ClientConstants.CONNECT_TIMEOUT);

		}
		return impl;
	}

	@Override
	public Result<Post> getPost(String postId) {
		try {
			return Result.ok(impl().getPost(postId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}


	@Override
	public Result<String> createPost(Post post) {
		try {
			return Result.ok(impl().createPost(post));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> deletePost(String postId) {
		try {
			impl().deletePost(postId);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		try {
			impl().like(postId, userId, isLiked);
			return Result.ok();
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		try {
			return Result.ok(impl().isLiked(postId, userId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		try {
			return Result.ok(impl().getPosts(userId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		try {
			return Result.ok(impl().getFeed(userId));
		} catch (MicrogramException e) {
			return Result.error(super.errorCode(e));
		}
	}
}
