package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class IndexExpression implements Expression<Object> {
    private final Expression<Indexable<?>> expression;
    private final Expression<Integer> index;

    public IndexExpression(Expression<Indexable<?>> expression, Expression<Integer> index) {
        this.expression = expression;
        this.index = index;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        return expression.evaluate(environment).getAtIndex(index.evaluate(environment));
    }
}
