package io.github.spencerpark.mellowd.intermediate.variables;

public class UndefinedReferenceException extends RuntimeException {
    private final String identifier;

    public UndefinedReferenceException(String identifier) {
        super(buildMessage(identifier));
        this.identifier = identifier;
    }

    public UndefinedReferenceException(String identifier, Throwable cause) {
        super(buildMessage(identifier), cause);
        this.identifier = identifier;
    }

    private static String buildMessage(String identifier) {
        return String.format("Identifier (%s) is undefined.", identifier);
    }

    public String getIdentifier() {
        return identifier;
    }
}
