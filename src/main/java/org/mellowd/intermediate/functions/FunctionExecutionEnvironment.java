package org.mellowd.intermediate.functions;

import org.mellowd.intermediate.variables.SymbolTable;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.NullMemory;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.compiler.MellowD;
import org.mellowd.compiler.PathMap;

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
