package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.SyncLink;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

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
            sourceLink.throwCompilationException(e);
        }
    }
}
