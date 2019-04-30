package microgram.impl.clt.java;

import microgram.api.java.Result;
import utils.Sleep;

import java.util.function.Supplier;

public abstract class RetryClient {

    public static final int READ_TIMEOUT = 5000;
    public static final int CONNECT_TIMEOUT = 3000;
	protected static final int RETRY_SLEEP = 500;
	private static final int MAX_RETRIES = Integer.MAX_VALUE;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (Exception x) {
                System.err.println("RetryException: " + x.getMessage());
                //x.printStackTrace();
                Sleep.ms(RETRY_SLEEP);
            }
        System.err.println("Max retries reached..." + func.toString());
        return Result.error(Result.ErrorCode.INTERNAL_ERROR);
    }

}
