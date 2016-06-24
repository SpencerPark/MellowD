package cas.cs4tb3.mellowd.intermediate.variables;

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
}
