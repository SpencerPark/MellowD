package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> names = new LinkedHashSet<>();
        this.operands.forEach(e -> names.addAll(e.getFreeVariables()));
        return names;
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
