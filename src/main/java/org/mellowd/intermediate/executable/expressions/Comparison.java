package org.mellowd.intermediate.executable.expressions;

import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.functions.operations.Comparable;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.primitives.*;

import java.util.*;
import java.util.function.BiFunction;

public class Comparison implements Expression<Boolean> {
    private final Expression<?> left;
    private final Comparable.Operator operator;
    private final Expression<?> right;

    public Comparison(Expression<?> left, Comparable.Operator operator, Expression<?> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        Set<QualifiedName> free = new LinkedHashSet<>(this.left.getFreeVariables());
        free.addAll(this.right.getFreeVariables());
        return free;
    }

    @Override
    public Boolean evaluate(ExecutionEnvironment environment) {
        Object left = this.left.evaluate(environment);
        Object right = this.right.evaluate(environment);

        //null == null, null < notNull
        if (left == null)
            if (right == null) return operator.isTrue(0);
            else               return operator.isTrue(-1);
        else if (right == null) return operator.isTrue(1);

        //Both non-null
        Class<?> leftType = left.getClass();
        Class<?> rightType = right.getClass();

        Class<?> compareType = leftType;
        if (leftType.isAssignableFrom(rightType))
            compareType = leftType;
        else if (rightType.isAssignableFrom(leftType))
            compareType = rightType;
        else
            throw new ClassCastException("Cannot compare " + leftType.getSimpleName() + " with " + rightType.getSimpleName());

        int cmp = compare(left, right, compareType);

        return operator.isTrue(cmp);
    }

    private static final Map<Class<?>, BiFunction<?, ?, Integer>> SUPPORTED_COMPARISONS = new HashMap<>();
    private static <T> void registerComparator(Class<T> type, BiFunction<T, T, Integer> comparator) {
        SUPPORTED_COMPARISONS.put(type, comparator);
    }
    private static <T> BiFunction<T, T, Integer> getComparator(Class<?> type) {
        return (BiFunction<T, T, Integer>) SUPPORTED_COMPARISONS.get(type);
    }
    static {
        registerComparator(Chord.class, Chord::compare);
        registerComparator(Melody.class, Melody::compare);
        registerComparator(Rhythm.class, Rhythm::compare);
        registerComparator(Pitch.class, Pitch::compare);
        registerComparator(Beat.class, Beat::compare);

        registerComparator(String.class, String::compareTo);
        registerComparator(Integer.class, Integer::compareTo);
        registerComparator(Integer.TYPE, Integer::compareTo);
        BiFunction<Boolean, Boolean, Integer> boolCmp = (left, right) ->
                left == right ? 0 : left ? 1 : -1;
        registerComparator(Boolean.class, boolCmp);
        registerComparator(Boolean.TYPE, boolCmp);
    }

    public static int compare(Object left, Object right, Class<?> type) {
        BiFunction<Object, Object, Integer> comparator = getComparator(type);
        if (comparator != null)
            return comparator.apply(left, right);

        throw new RuntimeException("Cannot compare " + type.getSimpleName() + "'s.");
    }
}
