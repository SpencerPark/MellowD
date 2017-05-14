package io.github.spencerpark.mellowd.intermediate.executable.expressions;

import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;
import io.github.spencerpark.mellowd.intermediate.functions.operations.Articulatable;
import io.github.spencerpark.mellowd.primitives.Articulated;
import io.github.spencerpark.mellowd.primitives.Articulation;

public class Articulate implements Expression<Articulated> {
    private final Expression<? extends Articulatable> expr;
    private final Expression<Articulation> articulation;

    public Articulate(Expression<? extends Articulatable> expr, Expression<Articulation> articulation) {
        this.expr = expr;
        this.articulation = articulation;
    }

    @Override
    public Articulated evaluate(ExecutionEnvironment environment) {
        Articulatable val = expr.evaluate(environment);
        return val.articulate(articulation.evaluate(environment));
    }
}
