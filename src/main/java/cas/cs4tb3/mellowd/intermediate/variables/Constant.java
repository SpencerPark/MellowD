package cas.cs4tb3.mellowd.intermediate.variables;

public class Constant<T> extends Reference<T> {

    public Constant(String identifier, Class<T> type, T value) {
        super(identifier, type, value);
    }

    @Override
    public boolean isDefined(Memory scope) {
        return true;
    }

    public void redefine(Memory scope, T newValue) {
        throw new UnsupportedOperationException("Cannot redefine a constant's value");
    }

    public T dereference(Memory scope) {
        return defaultValue;
    }
}
