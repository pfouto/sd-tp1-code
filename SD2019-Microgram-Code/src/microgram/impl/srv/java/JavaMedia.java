package microgram.impl.srv.java;

import microgram.api.java.Media;
import microgram.api.java.Result;
import utils.Hash;

import java.io.File;
import java.nio.file.Files;

public class JavaMedia implements Media {

	private static final String MEDIA_EXTENSION = ".jpg";
	private static final String ROOT_DIR = "/tmp/microgram/";

	private final String baseUri;

	public JavaMedia(String baseUri) {
		this.baseUri = baseUri;
		new File(ROOT_DIR).mkdirs();
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		try {
			String id = Hash.of(bytes);
			System.err.println("Upload over: " + id);
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
			if(!filename.exists()) 
				Files.write(filename.toPath(), bytes);
			return Result.ok(baseUri + "/" + id);
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<byte[]> download(String id) {
		try {
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
			if (filename.exists())
				return Result.ok(Files.readAllBytes(filename.toPath()));
			else
				return Result.error(Result.ErrorCode.NOT_FOUND);
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}

	@Override
	public Result<Void> delete(String id) {
		System.err.println("Delete over: " + id);
		try {
			File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
			if (filename.exists()) {
				Files.delete(filename.toPath());
				return Result.ok();
			} else
				return Result.error(Result.ErrorCode.NOT_FOUND);
		} catch (Exception x) {
			x.printStackTrace();
			return Result.error(Result.ErrorCode.INTERNAL_ERROR);
		}
	}
}
