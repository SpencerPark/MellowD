package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class IndexExpression implements Expression<Object> {
    private final Expression<? extends Indexable<?, ?>> expression;
    private final Expression<Integer> index;
    private final Expression<Integer> upperIndex;

    public IndexExpression(Expression<? extends Indexable<?, ?>> expression, Expression<Integer> index) {
        this.expression = expression;
        this.index = index;
        this.upperIndex = null;
    }

    public IndexExpression(Expression<? extends Indexable<?, ?>> expression, Expression<Integer> index, Expression<Integer> upperIndex) {
        this.expression = expression;
        this.index = index;
        this.upperIndex = upperIndex;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        Indexable<?, ?> result = expression.evaluate(environment);

        if (upperIndex == null)
            return result.getAtIndex(index.evaluate(environment));

        return result.getAtRange(index.evaluate(environment), upperIndex.evaluate(environment));
    }
}
