package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

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
    public TimingEnvironment getTimingEnvironment() {
        return wrapped.getTimingEnvironment();
    }
}
