package cas.cs4tb3.mellowd.parser;

import java.util.Collection;

public class ParseException extends RuntimeException {
    private final Collection<SyntaxErrorReport> problems;

    public ParseException(Collection<SyntaxErrorReport> problems) {
        this.problems = problems;
    }

    public Collection<SyntaxErrorReport> getProblems() {
        return problems;
    }
}
