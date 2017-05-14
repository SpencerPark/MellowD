package io.github.spencerpark.mellowd.parser;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.intermediate.variables.Memory;

public interface ExecutionEnvironment {

    boolean isPercussion();

    Memory getMemory(String... qualifier);

    Memory createScope(String... qualifier);

    TimingEnvironment getTimingEnvironment();
}
