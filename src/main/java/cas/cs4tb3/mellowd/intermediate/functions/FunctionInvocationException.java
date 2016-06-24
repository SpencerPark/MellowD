package cas.cs4tb3.mellowd.intermediate.functions;

/**
 * Created on 2016-06-17.
 */
public class FunctionInvocationException extends RuntimeException {
    public FunctionInvocationException() {
    }

    public FunctionInvocationException(String message) {
        super(message);
    }

    public FunctionInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
