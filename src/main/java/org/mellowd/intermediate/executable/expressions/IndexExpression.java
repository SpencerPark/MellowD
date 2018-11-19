package org.mellowd.intermediate.executable.expressions;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.functions.operations.Indexable;

import java.util.LinkedHashSet;
import java.util.Set;

public class IndexExpression implements Expression<Object> {
    private final Expression<? extends Indexable<?, ?>> expression;
    private final Expression<Integer> index;
    private final Expression<Integer> upperIndex;

    public IndexExpression(Expression<? extends Indexable<?, ?>> expression, Expression<Integer> index) {
        this.expression = expression;
        this.index = index;
        this.upperIndex = null;
    }

    public IndexExpression(Expression<? extends Indexable<?, ?>> expression, Expression<Integer> index, Expression<Integer> upperIndex) {
        this.expression = expression;
        this.index = index;
        this.upperIndex = upperIndex;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> free = new LinkedHashSet<>();

        free.addAll(this.expression.getFreeVariables());
        free.addAll(this.index.getFreeVariables());
        if (this.upperIndex != null)
            free.addAll(this.upperIndex.getFreeVariables());

        return free;
    }

    @Override
    public Object evaluate(ExecutionEnvironment environment) {
        Indexable<?, ?> result = expression.evaluate(environment);

        if (upperIndex == null)
            return result.getAtIndex(index.evaluate(environment));

        return result.getAtRange(index.evaluate(environment), upperIndex.evaluate(environment));
    }
}
