package cas.cs4tb3.mellowd.intermediate.variables;

public class AlreadyDefinedException extends RuntimeException {
    public AlreadyDefinedException(String message) {
        super(message);
    }

    public AlreadyDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
