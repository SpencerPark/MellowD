//Symbol Table
//============

package cas.cs4tb3.mellowd.parser;

import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.Map;

//A symbol table holds references. It is simply a Map at its core but it provides type
//and exception handling functionality.
public class SymbolTable {
    //The `declarations` are the actually `name -> value` mappings.
    private Map<String, Object> declarations;

    public SymbolTable() {
        declarations = new HashMap<>();
    }

    //`addDeclaration` is the only data input method for this class. It adds a
    //new mapping for the `identifier` to the `value`. It will overwrite an existing
    //data and will return true if it does so.
    public boolean addDeclaration(String identifier, Object value) {
        return this.declarations.put(identifier, value) != null;
    }

    //`getDeclarationValue` is the based data output method for this class. It takes
    //the name of the `identifier` to lookup and the expected type. If the value
    //does not exist this method simply returns null. If the value exists but is the wrong type
    //this method will treat the identifier as non-existent and return null. Otherwise the
    //value is returned.
    public <T> T getDeclarationValue(String identifier, Class<T> type) {
        Object value = declarations.get(identifier);
        return type.isInstance(value) ? (T) value : null;
    }

    //`getDeclarationValueOrThrow` is similar in function to `getDeclarationValue` but instead
    //of returning null, the appropriate exception will be thrown.
    public <T> T getDeclarationValueOrThrow(Token token, Class<T> type) {
        Object value = declarations.get(token.getText());
        //If the value is null then there is nothing defined so throw an
        //[UndefinedReferenceException](UndefinedReferenceException.html).
        if (value == null)
            throw new UndefinedReferenceException(token, "Identifier ("+token.getText()+") is undefined.");
        //If the type of the value is incorrect then something is defined but it is the wrong type
        //so throw an [IncorrectTypeException](IncorrectTypeException.html).
        if (!type.isInstance(value))
            throw new IncorrectTypeException(token, "Identifier ("+token.getText()+") points to a "+value.getClass().getSimpleName()+" not a "+type.getSimpleName());
        //Otherwise all is fine so we can safely cast and return the value
        return (T) value;
    }

    //`getType` is a utility method that will return the type of the data. This
    //will return null if the identifier is not defined.
    public Class<?> getType(String identifier) {
        Object value = declarations.get(identifier);
        return value == null ? null : value.getClass();
    }

    //`identifierTypeIs` preforms a type check on the identifier. It
    //will return true if the identifier is defined and points to an object
    //that can safely be cast to `type`
    public boolean identifierTypeIs(String identifier, Class<?> type) {
        Object value = declarations.get(identifier);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    //`checkType` is the equivalent of `identifierTypeIs` throwing an exception
    //if the type is defined and incorrect (not able to be cast to `type`).
    public void checkType(Token token, Class<?> type) {
        Object value = declarations.get(token.getText());
        if (value != null && !type.isAssignableFrom(value.getClass()))
            throw new IncorrectTypeException(token, "Identifier (" + token.getText() + ") points to a " + value.getClass().getSimpleName() + " not a " + type.getSimpleName());
    }

    //`checkExists` is a utility method that throws an exception if the identifier that
    //the `token` points to is not defined.
    public void checkExists(Token token) {
        Object value = declarations.get(token.getText());
        if (value == null)
            throw new UndefinedReferenceException(token, "Identifier ("+token.getText()+") is undefined.");
    }
}
