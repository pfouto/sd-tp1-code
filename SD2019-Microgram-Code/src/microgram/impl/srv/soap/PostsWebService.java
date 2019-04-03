package microgram.impl.srv.soap;

import javax.jws.WebService;

import microgram.api.soap.SoapPosts;

@WebService(serviceName=SoapPosts.NAME, targetNamespace=SoapPosts.NAMESPACE, endpointInterface=SoapPosts.INTERFACE)
public class PostsWebService {

}
