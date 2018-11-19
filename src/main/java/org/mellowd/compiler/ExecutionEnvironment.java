package org.mellowd.compiler;

import org.mellowd.intermediate.variables.Memory;
import org.mellowd.midi.TimingEnvironment;

public interface ExecutionEnvironment {

    boolean isPercussion();

    Memory getMemory();

    // TODO not necessary?
    Memory createScope(String... qualifier);

    TimingEnvironment getTimingEnvironment();
}
