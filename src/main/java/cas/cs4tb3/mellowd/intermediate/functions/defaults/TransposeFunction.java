package cas.cs4tb3.mellowd.intermediate.functions.defaults;

import cas.cs4tb3.mellowd.intermediate.functions.Arguments;
import cas.cs4tb3.mellowd.intermediate.functions.Function;
import cas.cs4tb3.mellowd.intermediate.functions.Parameter;
import cas.cs4tb3.mellowd.intermediate.functions.operations.Transposable;

public class TransposeFunction extends Function<Transposable> {
    private static final Parameter<Transposable> toTranspose = Parameter.newRequiredParameter("toTranspose", Transposable.class);
    private static final Parameter<Integer> amt = Parameter.newRequiredParameter("amt", Integer.class);

    public TransposeFunction() {
        super("transpose", toTranspose, amt);
    }

    @Override
    public Transposable<?> evaluate(Arguments arguments) {
        Transposable transposable = arguments.get(toTranspose);
        Integer shiftAmt = arguments.get(amt);

        return transposable.transpose(shiftAmt);
    }
}
