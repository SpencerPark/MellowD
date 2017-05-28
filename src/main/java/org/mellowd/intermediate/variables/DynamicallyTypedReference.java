package org.mellowd.intermediate.variables;

public class DynamicallyTypedReference extends Reference<Object> {

    public DynamicallyTypedReference(String identifier) {
        super(identifier, Object.class);
    }

    public DynamicallyTypedReference(String identifier, Object defaultValue) {
        super(identifier, Object.class, defaultValue);
    }

    public boolean typeIs(Memory scope, Class type) {
        return scope.identifierTypeIs(super.getIdentifier(), type);
    }

    @Override
    public boolean isDynamicallyTyped() {
        return true;
    }
}
