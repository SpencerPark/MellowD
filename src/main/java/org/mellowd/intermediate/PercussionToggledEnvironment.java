package org.mellowd.intermediate;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.midi.TimingEnvironment;

public class PercussionToggledEnvironment implements ExecutionEnvironment {
    private final ExecutionEnvironment wrapped;

    public PercussionToggledEnvironment(ExecutionEnvironment wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isPercussion() {
        return !wrapped.isPercussion();
    }

    @Override
    public Memory getMemory() {
        return wrapped.getMemory();
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return wrapped.getTimingEnvironment();
    }
}
