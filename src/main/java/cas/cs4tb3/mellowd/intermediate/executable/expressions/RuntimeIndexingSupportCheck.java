package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.parser.IndexingNotSupportedException;

public class RuntimeIndexingSupportCheck implements Expression<Indexable<?, ?>> {
    protected final Expression<?> expression;
    protected final SourceLink sourceLink;

    public RuntimeIndexingSupportCheck(Expression<?> expression, SourceLink sourceLink) {
        this.expression = expression;
        this.sourceLink = sourceLink;
    }

    @Override
    public Indexable<?, ?> evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || value instanceof Indexable) {
            return (Indexable) value;
        } else {
            throw sourceLink.toCompilationException(new IndexingNotSupportedException(sourceLink.text));
        }
    }
}
