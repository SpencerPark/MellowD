package cas.cs4tb3.mellowd;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016-03-07.
 */
public class TestErrorListener extends BaseErrorListener {
    private List<String> errors;

    public TestErrorListener() {
        this.errors = new ArrayList<>();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        this.errors.add("line " + line + ":" + charPositionInLine + " " + msg);
    }

    public boolean encounteredError() {
        return !this.errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }
}
