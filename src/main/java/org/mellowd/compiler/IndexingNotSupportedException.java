package org.mellowd.compiler;

public class IndexingNotSupportedException extends RuntimeException {
    public IndexingNotSupportedException(String message) {
        super(message);
    }

    public IndexingNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
