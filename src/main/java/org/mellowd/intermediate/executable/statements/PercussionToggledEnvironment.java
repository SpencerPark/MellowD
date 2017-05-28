package org.mellowd.intermediate.executable.statements;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.parser.ExecutionEnvironment;

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
    public Memory getMemory(String... qualifier) {
        return wrapped.getMemory(qualifier);
    }

    @Override
    public Memory createScope(String... qualifier) {
        return wrapped.createScope(qualifier);
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return wrapped.getTimingEnvironment();
    }
}
