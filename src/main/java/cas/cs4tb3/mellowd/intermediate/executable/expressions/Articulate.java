package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.primitives.Articulatable;
import cas.cs4tb3.mellowd.primitives.Articulated;
import cas.cs4tb3.mellowd.primitives.Articulation;

public class Articulate<T extends Articulatable> implements Expression<Articulated> {
    private final Expression<T> expr;
    private final Expression<Articulation> articulation;

    public Articulate(Expression<T> expr, Expression<Articulation> articulation) {
        this.expr = expr;
        this.articulation = articulation;
    }

    @Override
    public Articulated evaluate(ExecutionEnvironment environment) {
        T val = expr.evaluate(environment);
        return val.articulate(articulation.evaluate(environment));
    }
}
