package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Indexable;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.parser.IndexingNotSupportedException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class RuntimeIndexingSupportCheck extends SourceLink implements Expression<Indexable<?>> {
    protected final Expression<?> expression;

    public RuntimeIndexingSupportCheck(Expression<?> expression, ParserRuleContext tokenInfo) {
        super(tokenInfo);
        this.expression = expression;
    }

    public RuntimeIndexingSupportCheck(Expression<?> expression, TerminalNode tokenInfo) {
        super(tokenInfo);
        this.expression = expression;
    }

    @Override
    public Indexable<?> evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || value instanceof Indexable) {
            return (Indexable) value;
        } else {
            return throwCompilationException(new IndexingNotSupportedException(text));
        }
    }
}
