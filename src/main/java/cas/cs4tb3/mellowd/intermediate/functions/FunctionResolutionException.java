package cas.cs4tb3.mellowd.intermediate.functions;

public class FunctionResolutionException extends RuntimeException {
    private final String message;

    public FunctionResolutionException(FunctionBank.PercussionPair[] resolved, String name, boolean percussion, Argument<?>... arguments) {
        StringBuilder sb;
        if (resolved.length == 0) {
            sb = new StringBuilder("No functions match the signature described by ");
        } else {
            sb = new StringBuilder("Too many functions match the signature described by ");
        }

        sb.append(name);
        if (percussion) sb.append('*');
        sb.append("(");
        if (arguments.length > 0) {
            sb.append(arguments[0]);
            for (int i = 1; i < arguments.length; i++) sb.append(", ").append(arguments[i]);
        }
        sb.append(").");

        if (resolved.length > 0) {
            sb.append(" Did you mean: [");
            sb.append(resolved[0].getPreferredVariant(false).getSignature());
            for (int i = 1; i < resolved.length; i++) sb.append(", ").append(resolved[i].getPreferredVariant(false).getSignature());
            sb.append("]?");
        }

        this.message = sb.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return message;
    }
}
