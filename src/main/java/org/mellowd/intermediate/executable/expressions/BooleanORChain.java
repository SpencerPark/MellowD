package org.mellowd.intermediate.executable.expressions;

import org.mellowd.parser.ExecutionEnvironment;

import java.util.List;

/**
 * An {@link Expression} implementation for a chain of boolean AND
 * operations. This expression's {@link #evaluate(ExecutionEnvironment)}
 * algorithm implements short circuiting.
 */
public class BooleanORChain implements Expression<Boolean> {
    private List<Expression<Boolean>> operands;

    public BooleanORChain(List<Expression<Boolean>> operands) {
        this.operands = operands;
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        for (Expression<Boolean> expr : operands) {
            if (expr.evaluate(environment))
                return true; //One true makes the chain true, short circuit
        }

        //All were false
        return false;
    }
}
