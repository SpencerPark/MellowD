package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.functions.operations.Articulatable;
import org.mellowd.primitives.Articulated;
import org.mellowd.primitives.Articulation;

import java.util.Set;

public class Articulate implements Expression<Articulated> {
    private final Expression<? extends Articulatable> expr;
    private final Expression<Articulation> articulation;

    public Articulate(Expression<? extends Articulatable> expr, Expression<Articulation> articulation) {
        this.expr = expr;
        this.articulation = articulation;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return this.expr.getFreeVariables();
    }

    @Override
    public Articulated evaluate(ExecutionEnvironment environment) {
        Articulatable val = expr.evaluate(environment);
        return val.articulate(articulation.evaluate(environment));
    }
}
