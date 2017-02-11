package cas.cs4tb3.mellowd.intermediate.executable.expressions;

import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.List;

/**
 * An {@link Expression} implementation for a chain of boolean AND
 * operations. This expression's {@link #evaluate(ExecutionEnvironment)}
 * algorithm implements short circuiting.
 */
public class BooleanANDChain implements Expression<Boolean> {
    private List<Expression<Boolean>> operands;

    public BooleanANDChain(List<Expression<Boolean>> operands) {
        this.operands = operands;
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        for (Expression<Boolean> expr : operands) {
            if (!expr.evaluate(environment))
                return false; //One false makes the chain false, short circuit
        }

        //All were true
        return true;
    }
}
