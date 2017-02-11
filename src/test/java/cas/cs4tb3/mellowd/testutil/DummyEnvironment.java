package cas.cs4tb3.mellowd.testutil;

import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.NullMemory;
import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class DummyEnvironment implements ExecutionEnvironment {
    private static final DummyEnvironment env = new DummyEnvironment(false);
    private static final DummyEnvironment percussionEnv = new DummyEnvironment(true);

    public static DummyEnvironment getInstance() {
        return env;
    }

    public static DummyEnvironment getPercussionInstance() {
        return percussionEnv;
    }

    private final boolean isPercussion;
    private final Memory memory = new NullMemory();
    private final TimingEnvironment timingEnvironment = new TimingEnvironment(4, 4, 120);

    public DummyEnvironment(boolean isPercussion) {
        this.isPercussion = isPercussion;
    }

    @Override
    public boolean isPercussion() {
        return this.isPercussion;
    }

    @Override
    public Memory getMemory(String... qualifier) {
        return this.memory;
    }

    @Override
    public Memory createScope(String... qualifier) {
        return this.memory;
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return this.timingEnvironment;
    }
}
