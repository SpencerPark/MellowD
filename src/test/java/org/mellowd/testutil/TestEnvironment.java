package org.mellowd.testutil;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.SymbolTable;
import org.mellowd.midi.TimingEnvironment;

public class TestEnvironment implements ExecutionEnvironment {
    private final boolean percussion;
    private final Memory memory = new SymbolTable();
    private final TimingEnvironment timingEnvironment = new TimingEnvironment(4, 4, 120);

    public TestEnvironment(boolean percussion) {
        this.percussion = percussion;
    }

    public TestEnvironment() {
        this(false);
    }

    @Override
    public boolean isPercussion() {
        return this.percussion;
    }

    @Override
    public Memory getMemory() {
        return this.memory;
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return this.timingEnvironment;
    }
}
