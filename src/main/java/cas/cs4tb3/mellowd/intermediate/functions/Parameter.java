package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.variables.DynamicallyTypedReference;
import cas.cs4tb3.mellowd.intermediate.variables.Reference;

public class Parameter<T> {
    //TODO provide functionality for supplying default values or wrapping primitives etc

    public static <U> Parameter<U> newOptionalParameter(String name, Class<U> type) {
        return new Parameter<>(new Reference<>(name, type, null), true);
    }

    public static <U> Parameter<U> newRequiredParameter(String name, Class<U> type) {
        return new Parameter<>(new Reference<>(name, type, null), false);
    }

    public static Parameter<Object> newOptionalParameter(String name) {
        return new Parameter<>(new DynamicallyTypedReference(name), true);
    }

    public static Parameter<Object> newRequiredParameter(String name) {
        return new Parameter<>(new DynamicallyTypedReference(name), false);
    }

    private final Reference<T> reference;
    private final boolean isOptional;
    private final boolean isCollection;

    public Parameter(Reference<T> reference, boolean isOptional) {
        this.reference = reference;
        this.isOptional = isOptional;
        this.isCollection = reference.getType().isArray();
    }

    public Reference<T> getReference() {
        return reference;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public boolean isDynamicallyTyped() {
        return reference.isDynamicallyTyped();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getReference().toString());
        if (isCollection) sb.append("[]");
        if (isOptional) sb.append('?');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter<?> parameter = (Parameter<?>) o;

        if (isOptional != parameter.isOptional) return false;
        return reference.equals(parameter.reference);

    }

    @Override
    public int hashCode() {
        int result = reference.hashCode();
        result = 31 * result + (isOptional ? 1 : 0);
        return result;
    }
}
