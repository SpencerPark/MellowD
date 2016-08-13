package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.variables.IncorrectTypeException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class RuntimeTypeCheck<T> extends SourceLink implements Expression<T> {
    private final Class<T> type;
    private final Expression<?> expression;

    public RuntimeTypeCheck(Class<T> type, Expression<?> expression, ParserRuleContext tokenInfo) {
        super(tokenInfo);
        this.type = type;
        this.expression = expression;
    }

    public RuntimeTypeCheck(Class<T> type, Expression<?> expression, TerminalNode tokenInfo) {
        super(tokenInfo);
        this.type = type;
        this.expression = expression;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null || type.isAssignableFrom(value.getClass())) {
            return (T) value;
        } else {
            return throwCompilationException(new IncorrectTypeException(text, value.getClass(), type));
        }
    }
}
