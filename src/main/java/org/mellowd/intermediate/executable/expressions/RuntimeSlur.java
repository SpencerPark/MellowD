package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.functions.operations.Slurrable;

import java.util.LinkedHashSet;
import java.util.Set;

public class RuntimeSlur<T extends Slurrable<U>, U> implements Expression<U> {
    private final Expression<T> toSlur;

    public RuntimeSlur(Expression<T> toSlur) {
        this.toSlur = toSlur;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return new LinkedHashSet<>(this.toSlur.getFreeVariables());
    }

    @Override
    public U evaluate(ExecutionEnvironment environment) {
        T toSlur = this.toSlur.evaluate(environment);
        return toSlur.toggleSlur();
    }
}
