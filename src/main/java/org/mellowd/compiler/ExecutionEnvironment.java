package org.mellowd.compiler;

import org.mellowd.intermediate.variables.Memory;
import org.mellowd.midi.TimingEnvironment;

public interface ExecutionEnvironment {

    boolean isPercussion();

    Memory getMemory();

    TimingEnvironment getTimingEnvironment();
}
