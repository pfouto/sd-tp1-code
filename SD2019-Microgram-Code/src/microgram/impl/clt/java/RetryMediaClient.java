package microgram.impl.clt.java;

import microgram.api.java.Media;
import microgram.api.java.Result;
import utils.Sleep;


public class RetryMediaClient implements Media {

	final Media impl;

	public RetryMediaClient( Media impl ) {
		this.impl = impl;
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		for (;;)
			try {
				return impl.upload(bytes);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<byte[]> download(String id) {
		for (;;)
			try {
				return impl.download(id);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}

	@Override
	public Result<Void> delete(String id) {
		for (;;)
			try {
				return impl.delete(id);
			} catch (Exception x) {
				x.printStackTrace();
				Sleep.ms(Client.RETRY_SLEEP);
			}
	}
}