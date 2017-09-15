package org.mellowd.compiler;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.intermediate.variables.Memory;

public interface ExecutionEnvironment {

    boolean isPercussion();

    Memory getMemory(String... qualifier);

    Memory createScope(String... qualifier);

    TimingEnvironment getTimingEnvironment();
}
