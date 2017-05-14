package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.intermediate.Playable;
import io.github.spencerpark.mellowd.intermediate.executable.SourceLink;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

public class ContextFreeStatement implements Statement {
    private final Playable playable;
    private final SourceLink sourceLink;

    public ContextFreeStatement(SourceLink sourceLink, Playable playable) {
        this.sourceLink = sourceLink;
        this.playable = playable;
    }

    public Playable getPlayable() {
        return playable;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        try {
            output.put(playable);
        } catch (Exception e) {
            throw sourceLink.toCompilationException(e);
        }
    }
}
