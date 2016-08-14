package cas.cs4tb3.mellowd.intermediate.functions;

public class ArgumentException extends FunctionInvocationException {
    private final Parameter badParameter;
    private final String reason;

    public ArgumentException(Parameter badParameter, String reason) {
        super(buildMessage(badParameter, reason));
        this.badParameter = badParameter;
        this.reason = reason;
    }

    public ArgumentException(Parameter badParameter, String reason, Throwable cause) {
        super(buildMessage(badParameter, reason), cause);
        this.badParameter = badParameter;
        this.reason = reason;
    }

    private static String buildMessage(Parameter badParameter, String reason) {
        return String.format("Illegal argument for %s:%s. %s",
                badParameter.getReference().getType().getSimpleName(),
                badParameter.getReference().getIdentifier(),
                reason);
    }

    public Parameter getBadParameter() {
        return badParameter;
    }

    public String getReason() {
        return reason;
    }
}
