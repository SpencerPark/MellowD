package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.Playable;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.SourceLink;

import java.util.Collections;
import java.util.Set;

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
    public Set<QualifiedName> getFreeVariables() {
        return Collections.emptySet();
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
