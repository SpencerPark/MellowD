package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.intermediate.variables.IncorrectTypeException;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Preforms a type check but due to the lack of a union type it implements and
 * expression of object. This will throw an exception if the resolution is not
 * one of the desired types but it is up to the developer to do another type check
 * to determine the exact type from the types given.
 */
public class RuntimeUnionTypeCheck extends SourceLink implements Expression<Object> {
    private final Class<?>[] types;
    private final Expression<?> expression;

    public RuntimeUnionTypeCheck(Expression<?> expression, ParserRuleContext tokenInfo, Class<?>... types) {
        super(tokenInfo);
        this.expression = expression;
        this.types = types;
    }

    public RuntimeUnionTypeCheck(Expression<?> expression, TerminalNode tokenInfo, Class<?>... types) {
        super(tokenInfo);
        this.expression = expression;
        this.types = types;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        Object value = expression.evaluate(environment);
        if (value == null) return null;

        Class<?> valueType = value.getClass();
        for (Class<?> type : types) {
            if (type.isAssignableFrom(valueType)) return value;
        }

        return throwCompilationException(new IncorrectTypeException(text, value.getClass(), types));
    }
}
