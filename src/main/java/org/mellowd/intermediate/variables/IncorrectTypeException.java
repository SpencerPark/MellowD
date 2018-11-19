package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.QualifiedName;

import java.util.Arrays;
import java.util.stream.Collectors;

public class IncorrectTypeException extends RuntimeException {
    private final QualifiedName identifier;
    private final Class<?> actual;
    private final Class<?>[] expected;

    public IncorrectTypeException(QualifiedName identifier, Class<?> actual, Class<?>... expected) {
        super(buildMessage(identifier, actual, expected));
        this.identifier = identifier;
        this.actual = actual;
        this.expected = expected;
    }

    public IncorrectTypeException(QualifiedName identifier, Class<?> actual, Throwable cause, Class<?>... expected) {
        super(buildMessage(identifier, actual, expected), cause);
        this.identifier = identifier;
        this.actual = actual;
        this.expected = expected;
    }

    private static String buildMessage(QualifiedName identifier, Class<?> actual, Class<?>... expected) {
        return String.format("Identifier (%s) points to %s not an instance of %s.",
                identifier,
                actual != null ? "an instance of " + actual.getSimpleName() : "null",
                Arrays.stream(expected)
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ", "[", "]")));
    }

    public QualifiedName getIdentifier() {
        return identifier;
    }

    public Class<?> getActual() {
        return actual;
    }

    public Class<?>[] getExpected() {
        return expected;
    }
}
