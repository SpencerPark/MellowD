package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.QualifiedName;

public interface Memory {
    //`set` is the only data input method for this class. It adds a
    //new mapping for the `identifier` to the `value`. It will overwrite an existing
    //data and will return true if it does so.
    void set(QualifiedName identifier, Object value);

    void define(QualifiedName identifier, Object value);

    // `get` is the based data output method for this class. It takes
    // the name of the `identifier` to lookup and the expected type. If the value
    // does not exist this method simply returns null. If the value exists but is the wrong type
    // this method will treat the identifier as non-existent and return null. Otherwise the
    // value is returned.
    <T> T get(QualifiedName identifier, Class<T> type);

    Object get(QualifiedName identifier);

    boolean isDefined(QualifiedName identifier);

    //`getType` is a utility method that will return the type of the data. This
    //will return null if the identifier is not defined.
    Class<?> getType(QualifiedName identifier);

    //`identifierTypeIs` preforms a type check on the identifier. It
    //will return true if the identifier is defined and points to an object
    //that can safely be cast to `type`
    boolean identifierTypeIs(QualifiedName identifier, Class<?> type);

    int countReferences();

    String dump();
}
