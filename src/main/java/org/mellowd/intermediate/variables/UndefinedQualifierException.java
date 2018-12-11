package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.Qualifier;

public class UndefinedQualifierException extends RuntimeException {
    private final Qualifier qualifier;

    public UndefinedQualifierException(Qualifier qualifier) {
        super(buildMessage(qualifier));
        this.qualifier = qualifier;
    }

    public UndefinedQualifierException(Qualifier qualifier, Throwable cause) {
        super(buildMessage(qualifier), cause);
        this.qualifier = qualifier;
    }

    private static String buildMessage(Qualifier qualifier) {
        return String.format("Qualifier (%s) is undefined.", qualifier);
    }

    public Qualifier getQualifier() {
        return qualifier;
    }
}
