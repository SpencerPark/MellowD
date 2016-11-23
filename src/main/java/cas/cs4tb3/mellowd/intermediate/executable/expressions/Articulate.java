package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Articulatable;
import cas.cs4tb3.mellowd.primitives.Articulated;
import cas.cs4tb3.mellowd.primitives.Articulation;

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
