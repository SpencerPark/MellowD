package io.github.spencerpark.mellowd.intermediate.executable.statements;

import io.github.spencerpark.mellowd.intermediate.Output;
import io.github.spencerpark.mellowd.intermediate.SyncLink;
import io.github.spencerpark.mellowd.intermediate.executable.SourceLink;
import io.github.spencerpark.mellowd.parser.ExecutionEnvironment;

public class SyncStatement implements Statement {
    private final SourceLink sourceLink;
    private final SyncLink link;

    public SyncStatement(SourceLink sourceLink, SyncLink link) {
        this.sourceLink = sourceLink;
        this.link = link;
    }

    @Override
    public synchronized void execute(ExecutionEnvironment environment, Output output) {
        try {
            link.sync(output);
        } catch (InterruptedException e) {
            throw sourceLink.toCompilationException(e);
        }
    }
}
