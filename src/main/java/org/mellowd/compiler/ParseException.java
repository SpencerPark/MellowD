package org.mellowd.compiler;

import java.util.Collection;

public class ParseException extends RuntimeException {
    private final Collection<SyntaxErrorReport> problems;

    public ParseException(Collection<SyntaxErrorReport> problems) {
        this.problems = problems;
    }

    public Collection<SyntaxErrorReport> getProblems() {
        return problems;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        this.getProblems().forEach(err -> {
            sb.append(err.getErrorType().toString()).append(": ");
            sb.append(err.getMessage()).append("\n");
        });

        return sb.toString();
    }
}
