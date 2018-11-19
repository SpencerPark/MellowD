package org.mellowd.intermediate.executable.statements;

import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.Output;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.SyncLink;
import org.mellowd.intermediate.executable.SourceLink;

import java.util.Collections;
import java.util.Set;

public class SyncStatement implements Statement {
    private final SourceLink sourceLink;
    private final SyncLink link;

    public SyncStatement(SourceLink sourceLink, SyncLink link) {
        this.sourceLink = sourceLink;
        this.link = link;
    }

    @Override
    public Set<QualifiedName> getFreeVariables() {
        return Collections.emptySet();
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
