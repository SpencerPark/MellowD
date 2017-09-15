package org.mellowd.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SourceResolutionException extends Exception implements Iterable<String> {
    private final String[] importPath;
    private final List<String> searchErrors;

    public SourceResolutionException(String[] importPath, String finderErrorMessage) {
        super();
        this.importPath = importPath;
        this.searchErrors = Collections.singletonList(finderErrorMessage);
    }

    public SourceResolutionException(String[] importPath, List<String> finderErrorMessages) {
        super();
        this.importPath = importPath;
        this.searchErrors = finderErrorMessages;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder("Cannot resolve import: ");
        message.append(String.join(".", this.importPath));

        for (String error : searchErrors) {
            message.append("\n    - ");
            message.append(error);
        }

        return message.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return this.searchErrors.iterator();
    }
}
