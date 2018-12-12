package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.Qualifier;

public interface Memory {
    //`set` is the only data input method for this class. It adds a
    //new mapping for the `identifier` to the `value`. It will overwrite an existing
    //data and will return true if it does so.
    default void set(QualifiedName identifier, Object value) {
        this.lookupOrCreateNamespace(identifier.getQualifier())
                .set(identifier.getName(), value);
    }

    void set(String name, Object value);

    default void define(QualifiedName identifier, Object value) {
        this.lookupOrCreateNamespace(identifier.getQualifier())
                .define(identifier.getName(), value);
    }

    void define(String name, Object value);

    // `get` is the based data output method for this class. It takes
    // the name of the `identifier` to lookup and the expected type. If the value
    // does not exist this method simply returns null. If the value exists but is the wrong type
    // this method will treat the identifier as non-existent and return null. Otherwise the
    // value is returned.
    default <T> T get(QualifiedName identifier, Class<T> type) {
        Object value = this.get(identifier);

        return type.isInstance(value) ? (T) value : null;
    }

    default <T> T get(String name, Class<T> type) {
        Object value = this.get(name);

        return type.isInstance(value) ? (T) value : null;
    }

    default Object get(QualifiedName identifier) {
        Memory memory = this.lookupNamespace(identifier.getQualifier());
        if (memory == null) return null;

        return memory.get(identifier.getName());
    }

    Object get(String name);

    default boolean isDefined(QualifiedName identifier) {
        Memory memory = this.lookupNamespace(identifier.getQualifier());
        if (memory == null) return false;

        return memory.isDefined(identifier.getName());
    }

    boolean isDefined(String name);

    //`getType` is a utility method that will return the type of the data. This
    //will return null if the identifier is not defined.
    default Class<?> getType(QualifiedName identifier) {
        Object value = this.get(identifier);
        return value == null ? null : value.getClass();
    }

    //`identifierTypeIs` preforms a type check on the identifier. It
    //will return true if the identifier is defined and points to an object
    //that can safely be cast to `type`
    default boolean identifierTypeIs(QualifiedName identifier, Class<?> type) {
        Object value = this.get(identifier);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    default void setNamespace(QualifiedName identifier, Memory namespace) {
        Memory scope = this;

        for (String name : identifier.getQualifier().getPath())
            scope = scope.lookupOrCreateNamespace(name);

        scope.setNamespace(identifier.getName(), namespace);
    }

    void setNamespace(String name, Memory namespace);

    default Memory lookupOrCreateNamespace(Qualifier identifier) {
        Memory scope = this;
        for (String name : identifier.getPath())
            scope = scope.lookupOrCreateNamespace(name);

        return scope;
    }

    Memory lookupOrCreateNamespace(String name);

    default Memory lookupNamespace(Qualifier identifier) {
        Memory scope = this;
        for (String name : identifier.getPath()) {
            scope = scope.lookupNamespace(name);
            if (scope == null)
                break;
        }

        return scope;
    }

    Memory lookupNamespace(String name);

    int countReferences();

    String dump();
}
