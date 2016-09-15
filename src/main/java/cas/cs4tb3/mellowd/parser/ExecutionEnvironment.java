package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;

public interface ExecutionEnvironment {

    boolean isPercussion();

    Memory getMemory(String... qualifier);

    Memory createScope(String... qualifier);

    TimingEnvironment getTimingEnvironment();
}
