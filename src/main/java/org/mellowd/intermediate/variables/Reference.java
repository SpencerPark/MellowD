package org.mellowd.intermediate.variables;

public class Reference<T> {
    protected final String identifier;
    protected final Class<T> type;
    protected final T defaultValue;

    public Reference(String identifier, Class<T> type, T defaultValue) {
        this.identifier = identifier;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public Reference(String identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
        this.defaultValue = null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getType() {
        return type;
    }

    public boolean isDefined(Memory scope) {
        return scope.get(identifier) != null;
    }

    public void redefine(Memory scope, T newValue) {
        scope.set(identifier, newValue);
    }

    public T dereference(Memory scope) {
        T value = scope.get(identifier, type);
        return value == null ? defaultValue : value;
    }

    public void putDefault(Memory scope) {
        scope.set(identifier, defaultValue);
    }

    public boolean isDynamicallyTyped() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(identifier);
        if (!isDynamicallyTyped()) sb.append(':').append(type.getSimpleName());
        if (defaultValue != null) sb.append('=').append(defaultValue.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reference<?> reference = (Reference<?>) o;

        if (!identifier.equals(reference.identifier)) return false;
        return type.equals(reference.type);

    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
