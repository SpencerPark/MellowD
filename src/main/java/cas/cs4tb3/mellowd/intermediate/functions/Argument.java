package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;

public class Argument<T> {
    private static final Argument<?> EMPTY_ARG_INSTANCE = new Argument<>(null, null);
    public static <T> Argument<T> getEmptyArgInstance() {
        return (Argument<T>) EMPTY_ARG_INSTANCE;
    }

    private final String name;
    private final Expression<T> value;

    public Argument(String name, Expression<T> value) {
        this.name = name;
        this.value = value;
    }

    public Argument(Expression<T> value) {
        this.name = null;
        this.value = value;
    }

    public Argument(String name) {
        this.name = name;
        this.value = null;
    }

    public String getName() {
        return name;
    }

    public boolean isNamed() {
        return this.name != null;
    }

    public Expression<T> getValue() {
        return value;
    }

    public boolean isDeclaredNull() {
        return this.value == null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isNamed()) sb.append(name).append(':');
        if (isDeclaredNull()) sb.append("null");
        else                  sb.append(value);
        return sb.toString();
    }
}
