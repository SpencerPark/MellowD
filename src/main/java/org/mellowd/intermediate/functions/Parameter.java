package org.mellowd.intermediate.functions;

import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.expressions.Expression;
import org.mellowd.intermediate.variables.IncorrectTypeException;
import org.mellowd.intermediate.variables.Memory;

import java.util.Objects;

public class Parameter<T> {
    // TODO provide functionality for wrapping primitives etc

    public static <U> Parameter<U> newRequiredParameter(String name, Class<U> type) {
        return new Parameter<>(name, type, null, false);
    }

    public static <U> Parameter<U> newOptionalParameter(String name, Class<U> type) {
        return new Parameter<>(name, type, null, true);
    }

    public static <U> Parameter<U> newParameterWithDefault(String name, Class<U> type, Expression<U> value) {
        return new Parameter<>(name, type, value, true);
    }

    public static Parameter<Object> newRequiredParameter(String name) {
        return new Parameter<>(name, null, null, false);
    }

    public static Parameter<Object> newOptionalParameter(String name) {
        return new Parameter<>(name, null, null, true);
    }

    public static Parameter<Object> newParameterWithDefault(String name, Expression<Object> value) {
        return new Parameter<>(name, null, value, true);
    }

    private final String name;
    private final Class<T> type;
    private final Expression<T> defaultValue;
    private final boolean isOptional;

    private Parameter(String name, Class<T> type, Expression<T> defaultValue, boolean isOptional) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.isOptional = isOptional;
    }

    public String getName() {
        return this.name;
    }

    public QualifiedName getNameAsQualified() {
        return QualifiedName.ofUnqualified(this.name);
    }

    public Class<T> getType() {
        // This cast is ok because if a type is not given during construction then this is
        // dynamically typed and the T must be Object.
        return this.type == null ? (Class<T>) Object.class : this.type;
    }

    public boolean isDynamicallyTyped() {
        return this.type == null;
    }

    public boolean isOptional() {
        return this.isOptional;
    }

    public boolean hasDefualtValue() {
        return this.defaultValue != null;
    }

    public Expression<T> getDefaultValue() {
        return this.defaultValue;
    }

    public void checkIsAssignable(Object value) {
        if (this.isDynamicallyTyped())
            return;

        if (this.getType().isInstance(value))
            return;

        throw new IncorrectTypeException(this.getNameAsQualified(), value.getClass(), this.getType());
    }

    public T dereference(Memory memory) {
        if (this.isDynamicallyTyped())
            return (T) memory.get(this.getNameAsQualified());
        else
            return memory.get(this.getNameAsQualified(), this.getType());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (!this.isDynamicallyTyped())
            sb.append(this.getType().getSimpleName()).append(" ");

        sb.append(this.getName());

        if (this.isOptional())
            sb.append('?');

        if (this.hasDefualtValue())
            sb.append(" -> ").append(this.getDefaultValue().toString());

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter<?> parameter = (Parameter<?>) o;
        return isOptional == parameter.isOptional &&
                Objects.equals(name, parameter.name) &&
                Objects.equals(type, parameter.type) &&
                Objects.equals(defaultValue, parameter.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, isOptional);
    }
}
