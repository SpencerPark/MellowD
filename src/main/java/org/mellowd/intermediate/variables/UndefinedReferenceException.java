package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.QualifiedName;

public class UndefinedReferenceException extends RuntimeException {
    private final QualifiedName identifier;

    public UndefinedReferenceException(QualifiedName identifier) {
        super(buildMessage(identifier));
        this.identifier = identifier;
    }

    public UndefinedReferenceException(QualifiedName identifier, Throwable cause) {
        super(buildMessage(identifier), cause);
        this.identifier = identifier;
    }

    private static String buildMessage(QualifiedName identifier) {
        return String.format("Identifier (%s) is undefined.", identifier);
    }

    public QualifiedName getIdentifier() {
        return identifier;
    }
}
