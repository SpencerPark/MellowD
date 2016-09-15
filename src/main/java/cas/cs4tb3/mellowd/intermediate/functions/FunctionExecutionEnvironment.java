package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;
import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.NullMemory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import cas.cs4tb3.mellowd.parser.MellowD;
import cas.cs4tb3.mellowd.parser.PathMap;

public class FunctionExecutionEnvironment implements ExecutionEnvironment {
    public static final String RETURN_QUALIFIER = "return";

    private final MellowD global;
    private final ExecutionEnvironment executingIn;
    private final boolean isPercussion;

    private final PathMap<Memory> functionMemory;

    public FunctionExecutionEnvironment(MellowD global, ExecutionEnvironment executingIn, Memory arguments, String name,  boolean shouldReturn, boolean percussion) {
        this.global = global;
        this.executingIn = executingIn;
        this.isPercussion = percussion;

        this.functionMemory = new PathMap<>(arguments);
        Memory returnMemory = shouldReturn ? executingIn.createScope(name) : NullMemory.getInstance();
        this.functionMemory.put(returnMemory, RETURN_QUALIFIER);
    }

    @Override
    public boolean isPercussion() {
        return isPercussion;
    }

    @Override
    public Memory getMemory(String... qualifier) {
        Memory memory = functionMemory.get(qualifier);
        return memory != null ? memory : global.getMemory(qualifier);
    }

    @Override
    public Memory createScope(String... qualifier) {
        return functionMemory.putIfAbsent(SymbolTable::new, qualifier);
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return executingIn.getTimingEnvironment();
    }
}
