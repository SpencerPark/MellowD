package io.github.spencerpark.mellowd.intermediate.functions;

import io.github.spencerpark.mellowd.intermediate.variables.SymbolTable;
import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.intermediate.variables.Memory;
import io.github.spencerpark.mellowd.intermediate.variables.NullMemory;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;
import io.github.spencerpark.mellowd.parser.MellowD;
import io.github.spencerpark.mellowd.parser.PathMap;

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
