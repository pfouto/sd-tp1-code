package microgram.impl.srv.java;

import microgram.api.java.Media;
import microgram.api.java.Result;
import utils.Hash;

import java.io.File;
import java.nio.file.Files;

import static microgram.api.java.Result.ErrorCode.INTERNAL_ERROR;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;
import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;

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
            File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
            Files.write(filename.toPath(), bytes);
            return ok(baseUri + "/" + id);
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String id) {
        try {
            File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
            if (filename.exists())
                return ok(Files.readAllBytes(filename.toPath()));
            else
                return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> delete(String id) {
        try {
            File filename = new File(ROOT_DIR + id + MEDIA_EXTENSION);
            if (filename.exists()) {
                Files.delete(filename.toPath());
                return ok();
            } else
                return error(NOT_FOUND);
        } catch (Exception x) {
            x.printStackTrace();
            return error(INTERNAL_ERROR);
        }
    }
}
