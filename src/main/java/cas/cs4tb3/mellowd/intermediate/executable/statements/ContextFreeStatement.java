package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.Playable;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

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
