//Undefined Reference Exception
//=============================

package cas.cs4tb3.mellowd.parser;

import org.antlr.v4.runtime.Token;

//This exception is the equivalent of a null pointer exception. If an identifier doesn't point
//to anything an undefined reference exception may be thrown. It extends a Parse Exception so that
//it can reference the token that caused the problem.
public class UndefinedReferenceException extends ParseException {
    public UndefinedReferenceException(Token token) {
        super(token);
    }

    public UndefinedReferenceException(Token token, String message) {
        super(token, message);
    }

    public UndefinedReferenceException(int start, int posInLine, int lineNum, String text) {
        super(start, posInLine, lineNum, text);
    }

    public UndefinedReferenceException(int start, int posInLine, int lineNum, String text, String message) {
        super(start, posInLine, lineNum, text, message);
    }
}
