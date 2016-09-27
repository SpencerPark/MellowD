package cas.cs4tb3.mellowd.intermediate.functions.defaults;

import cas.cs4tb3.mellowd.intermediate.TransposeChange;
import cas.cs4tb3.mellowd.intermediate.functions.Function;
import cas.cs4tb3.mellowd.intermediate.functions.FunctionSignature;
import cas.cs4tb3.mellowd.intermediate.functions.Parameter;

public class TransposeFunction extends Function {
    private static final Parameter<Number> numSemiTones = Parameter.newRequiredParameter("transposeAmt", Number.class);

    private static TransposeFunction instance;
    private static TransposeFunction percussionInstance;

    public static TransposeFunction getInstance(boolean percussion) {
        if (percussion) {
            if (percussionInstance == null)
                percussionInstance = new TransposeFunction(true);
            return percussionInstance;
        } else {
            if (instance == null)
                instance = new TransposeFunction();
            return instance;
        }
    }

    private TransposeFunction() {
        super(new FunctionSignature("transpose", numSemiTones), false, (env, out) -> {
            Number shiftAmt = numSemiTones.getReference().dereference(env.getMemory());
            out.put(new TransposeChange(shiftAmt.intValue()));
        });
    }

    private TransposeFunction(boolean dummy) {
        super(new FunctionSignature("transpose", numSemiTones), true, (env, out) -> {
            throw new IllegalStateException("Cannot transpose a percussion sound.");
        });
    }
}
