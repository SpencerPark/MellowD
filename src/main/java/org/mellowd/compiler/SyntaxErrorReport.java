package org.mellowd.compiler;

public class SyntaxErrorReport {
    public enum ErrorType {
        NO_VIABLE_ALTERNATIVE,
        INPUT_MISMATCH,
        FAILED_PREDICATE,
        OTHER
    }

    private final ErrorType errorType;
    private final String message;

    public SyntaxErrorReport(ErrorType errorType, String message) {
        this.errorType = errorType;
        this.message = message;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }
}
