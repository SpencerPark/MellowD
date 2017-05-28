package org.mellowd.intermediate.executable.expressions;

import org.mellowd.parser.ExecutionEnvironment;

public class BooleanNotExpression implements Expression<Boolean> {
    private final Expression<Boolean> expr;

    public BooleanNotExpression(Expression<Boolean> expr) {
        this.expr = expr;
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        return !expr.evaluate(environment);
    }
}
