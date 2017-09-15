package org.mellowd.intermediate.executable.expressions;

import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.intermediate.variables.IncorrectTypeException;
import org.mellowd.compiler.ExecutionEnvironment;

/**
 * Preforms a type check but due to the lack of a union type it implements and
 * expression of object. This will throw an exception if the resolution is not
 * one of the desired types but it is up to the developer to do another type check
 * to determine the exact type from the types given.
 */
public class RuntimeUnionTypeCheck implements Expression<Object> {
    private final Class<?>[] types;
    private final Expression<?> expression;
    private final SourceLink sourceLink;

    public RuntimeUnionTypeCheck(Expression<?> expression, Class<?>[] types, SourceLink sourceLink) {
        this.expression = expression;
        this.types = types;
        this.sourceLink = sourceLink;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null) return null;

        Class<?> valueType = value.getClass();
        for (Class<?> type : types) {
            if (type.isAssignableFrom(valueType)) return value;
        }

        throw sourceLink.toCompilationException(new IncorrectTypeException(sourceLink.text, value.getClass(), types));
    }
}
