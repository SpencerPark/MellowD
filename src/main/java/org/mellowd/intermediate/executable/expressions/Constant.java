package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;

public class Constant<T> implements Expression<T> {
    private final T value;

    public Constant(T val) {
        this.value = val;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
