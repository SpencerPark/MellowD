//Incorrect Type Exception
//========================

package cas.cs4tb3.mellowd.parser;

import org.antlr.v4.runtime.Token;

//An incorrect type exception is an extension of a [ParseException](ParseException.html) in
//the sense it should be specified with a token for reference to the erroneous reference.

//Incorrect type exceptions are thrown when an identifier exists but is not of the expected or
//desired type.
public class IncorrectTypeException extends ParseException {
    public IncorrectTypeException(Token token) {
        super(token);
    }

    public IncorrectTypeException(Token token, String message) {
        super(token, message);
    }

    public IncorrectTypeException(int start, int posInLine, int lineNum, String text) {
        super(start, posInLine, lineNum, text);
    }

    public IncorrectTypeException(int start, int posInLine, int lineNum, String text, String message) {
        super(start, posInLine, lineNum, text, message);
    }
}
