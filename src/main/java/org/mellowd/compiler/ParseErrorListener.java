package org.mellowd.compiler;

import org.antlr.v4.runtime.*;

import java.util.LinkedList;
import java.util.List;

public class ParseErrorListener extends BaseErrorListener {
    private List<SyntaxErrorReport> errors;

    public ParseErrorListener() {
        this.errors = new LinkedList<>();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (e instanceof NoViableAltException) {
            switch (e.getOffendingToken().getType()) {
                case MellowDParser.BRACE_OPEN:
                    msg += ". Are you missing a '}'?";
                    break;
                case MellowDParser.BRACKET_OPEN:
                    msg += ". Are you missing a ']'?";
                    break;
                case MellowDParser.P_BRACKET_OPEN:
                    msg += ". Are you missing a '>'?";
                    break;
                case MellowDParser.PAREN_OPEN:
                    msg += ". Are you missing a ')'?";
                    break;
            }
            this.errors.add(new SyntaxErrorReport(SyntaxErrorReport.ErrorType.NO_VIABLE_ALTERNATIVE,
                    buildErrorMessage(e.getOffendingToken(), msg)));
        } else if (e instanceof InputMismatchException) {
            this.errors.add(new SyntaxErrorReport(SyntaxErrorReport.ErrorType.INPUT_MISMATCH,
                    buildErrorMessage(e.getOffendingToken(), msg)));
        } else if (e instanceof FailedPredicateException) {
            this.errors.add(new SyntaxErrorReport(SyntaxErrorReport.ErrorType.FAILED_PREDICATE,
                    buildErrorMessage(e.getOffendingToken(), msg)));
        } else {
            this.errors.add(new SyntaxErrorReport(SyntaxErrorReport.ErrorType.OTHER,
                    String.format("line %d@%d. Problem: %s",
                            line, charPositionInLine, msg)));
        }
    }

    private static String buildErrorMessage(Token problem, String message) {
        return String.format("line %d@%d-%d:'%s'. Problem: %s",
                problem.getLine(),
                problem.getCharPositionInLine(),
                problem.getCharPositionInLine() + (problem.getStopIndex() - problem.getStartIndex()),
                problem.getText(),
                message);
    }

    public boolean encounteredError() {
        return !this.errors.isEmpty();
    }

    public List<SyntaxErrorReport> getErrors() {
        return errors;
    }
}
