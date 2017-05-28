//Test Error Listener
//===================

package org.mellowd;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.List;

//This error listener simply remembers the errors that occurred during parsing.
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
