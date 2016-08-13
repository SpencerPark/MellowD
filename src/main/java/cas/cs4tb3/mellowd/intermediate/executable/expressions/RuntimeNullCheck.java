package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.variables.UndefinedReferenceException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class RuntimeNullCheck<T> extends SourceLink implements Expression<T> {
    private final String referenceName;
    private final Expression<T> expression;

    public RuntimeNullCheck(String referenceName, Expression<T> expression, ParserRuleContext tokenInfo) {
        super(tokenInfo);
        this.referenceName = referenceName;
        this.expression = expression;
    }

    public RuntimeNullCheck(String referenceName, Expression<T> expression, TerminalNode tokenInfo) {
        super(tokenInfo);
        this.referenceName = referenceName;
        this.expression = expression;
    }

    @Override
    public T evaluate(ExecutionEnvironment environment) {
        T value = expression.evaluate(environment);
        if (value == null)
            return throwCompilationException(new UndefinedReferenceException(referenceName));
        return value;
    }
}
