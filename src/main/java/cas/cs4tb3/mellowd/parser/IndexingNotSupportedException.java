package cas.cs4tb3.mellowd.parser;

/**
 * Created on 2016-06-21.
 */
public class IndexingNotSupportedException extends RuntimeException {
    public IndexingNotSupportedException(String message) {
        super(message);
    }

    public IndexingNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
