package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.variables.UndefinedReferenceException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class RuntimeNullCheck<T> implements Expression<T> {
    private final String referenceName;
    private final Expression<T> expression;
    private final SourceLink sourceLink;

    public RuntimeNullCheck(String referenceName, Expression<T> expression, SourceLink sourceLink) {
        this.referenceName = referenceName;
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T value = expression.evaluate(environment);
        if (value == null)
            throw sourceLink.toCompilationException(new UndefinedReferenceException(referenceName));
        return value;
    }
}
