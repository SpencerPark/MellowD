package cas.cs4tb3.mellowd.intermediate.variables;

public class DynamicallyTypedReference extends Reference<Object> {

    public DynamicallyTypedReference(String identifier) {
        super(identifier, Object.class, null);
    }

    public boolean typeIs(Memory scope, Class type) {
        return scope.identifierTypeIs(super.getIdentifier(), type);
    }

    public <T> T getAs(Memory scope, Class<T> type) {
        return scope.getOrThrow(super.getIdentifier(), type);
    }
}
