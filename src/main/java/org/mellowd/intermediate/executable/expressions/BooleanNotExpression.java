package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;

import java.util.Set;

public class BooleanNotExpression implements Expression<Boolean> {
    private final Expression<Boolean> expr;

    public BooleanNotExpression(Expression<Boolean> expr) {
        this.expr = expr;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.expr.getFreeVariables();
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        return !expr.evaluate(environment);
    }
}
