package org.mellowd.intermediate;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.midi.TimingEnvironment;

public class ClosureEvaluationEnvironment implements ExecutionEnvironment {
    private final ExecutionEnvironment callerEnv;
    private final Memory scope;
    private final boolean percussion;

    public ClosureEvaluationEnvironment(ExecutionEnvironment callerEnv, Memory scope, boolean percussion) {
        this.callerEnv = callerEnv;
        this.scope = scope;
        this.percussion = percussion;
    }

    @Override
    public boolean isPercussion() {
        return this.percussion;
    }

    @Override
    public Memory getMemory() {
        return this.scope;
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return this.callerEnv.getTimingEnvironment();
    }
}
