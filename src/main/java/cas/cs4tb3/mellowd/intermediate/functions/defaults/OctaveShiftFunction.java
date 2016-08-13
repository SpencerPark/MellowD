package cas.cs4tb3.mellowd.intermediate.functions.defaults;

import cas.cs4tb3.mellowd.intermediate.OctaveShift;
import cas.cs4tb3.mellowd.intermediate.functions.Function;
import cas.cs4tb3.mellowd.intermediate.functions.FunctionSignature;
import cas.cs4tb3.mellowd.intermediate.functions.Parameter;

public class OctaveShiftFunction extends Function {
    private static final Parameter<Number> shiftAmtParam = Parameter.newRequiredParameter("shiftAmt", Number.class);

    private static OctaveShiftFunction instance;
    private static OctaveShiftFunction percussionInstance;

    public static OctaveShiftFunction getInstance(boolean percussion) {
        if (percussion) {
            if (percussionInstance == null)
                percussionInstance = new OctaveShiftFunction(true);
            return percussionInstance;
        } else {
            if (instance == null)
                instance = new OctaveShiftFunction();
            return instance;
        }
    }

    private OctaveShiftFunction() {
        super(new FunctionSignature("octave", shiftAmtParam), false, (env, out) -> {
            Number shiftAmt = shiftAmtParam.getReference().dereference(env.getMemory());
            out.put(new OctaveShift(shiftAmt.intValue()));
        });
    }

    private OctaveShiftFunction(boolean dummy) {
        super(new FunctionSignature("octave", shiftAmtParam), true, (env, out) -> {
            throw new IllegalStateException("Cannot shift the octave of a percussion sound.");
        });
    }
}
