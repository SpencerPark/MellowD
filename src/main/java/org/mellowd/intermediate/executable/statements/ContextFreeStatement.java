package org.mellowd.intermediate.executable.statements;

import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.Playable;
import org.mellowd.intermediate.executable.SourceLink;
import org.mellowd.compiler.ExecutionEnvironment;

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
