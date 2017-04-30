package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.primitives.Pitch;

public class BooleanEvaluationExpression implements Expression<Boolean> {
    private final Expression<?> expression;

    public BooleanEvaluationExpression(Expression<?> expression) {
        this.expression = expression;
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        Object val = expression.evaluate(environment);
        if (val instanceof Pitch) {
            return val != Pitch.REST;
        } else if (val instanceof Number) {
            return ((Number) val).intValue() > 0;
        } else if (val instanceof Boolean) {
            return (Boolean) val;
        } else {
            return val != null;
        }
    }
}