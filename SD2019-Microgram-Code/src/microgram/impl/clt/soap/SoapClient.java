package microgram.impl.clt.soap;


import java.net.URI;
import java.net.URL;

import microgram.api.java.Result.ErrorCode;
import microgram.api.soap.MicrogramException;

abstract class SoapClient {

	private static final String WSDL = "?wsdl";
	protected final URI uri;

	public SoapClient(URI uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString() {
		return uri.toString();
	}
	
	protected URL wsdl() {
		try {
			return new URL(uri.toString() + WSDL);
		} catch (Exception x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	//Translates the MicrogramException into an ErrorCode
	static protected ErrorCode errorCode(MicrogramException me) {
		switch (me.getMessage()) {
			case "OK": return ErrorCode.OK;
			case "CONFLICT" : return ErrorCode.CONFLICT;
			case "NOT_FOUND": return ErrorCode.NOT_FOUND;
			case "NOT_IMPLEMENTED": return ErrorCode.NOT_IMPLEMENTED;
			case "INTERNAL_ERROR":
			default:
			return ErrorCode.INTERNAL_ERROR;
		}
	}
}
