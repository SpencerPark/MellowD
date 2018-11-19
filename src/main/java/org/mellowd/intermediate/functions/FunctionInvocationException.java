package org.mellowd.intermediate.functions;

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
