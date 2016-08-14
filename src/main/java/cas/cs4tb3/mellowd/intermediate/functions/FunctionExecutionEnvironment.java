package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.NullMemory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class FunctionExecutionEnvironment implements ExecutionEnvironment {
    public static final String RETURN_QUALIFIER = "return";
    private final ExecutionEnvironment executingIn;
    private final Memory functionMemory;
    private final Memory returnMemory;
    private final boolean isPercussion;

    public FunctionExecutionEnvironment(ExecutionEnvironment executingIn, Memory arguments, boolean shouldReturn, boolean percussion) {
        this.executingIn = executingIn;
        this.functionMemory = arguments;
        this.returnMemory = shouldReturn ? executingIn.getMemory() : NullMemory.getInstance();
        this.isPercussion = percussion;
    }

    @Override
    public boolean isPercussion() {
        return isPercussion;
    }

    @Override
    public Memory getMemory(String... qualifier) {
        if (qualifier.length == 0)
            return functionMemory;
        if (qualifier.length == 1 && RETURN_QUALIFIER.equals(qualifier[0]))
            return returnMemory;
        return executingIn.getMemory(qualifier);
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return executingIn.getTimingEnvironment();
    }
}
