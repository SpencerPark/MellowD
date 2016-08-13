package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.Playable;
import cas.cs4tb3.mellowd.intermediate.executable.SourceLink;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ContextFreeStatement extends SourceLink implements Statement {
    private final Playable playable;

    public ContextFreeStatement(ParserRuleContext info, Playable playable) {
        super(info);
        this.playable = playable;
    }

    public ContextFreeStatement(TerminalNode info, Playable playable) {
        super(info);
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
            throwCompilationException(e);
        }
    }
}
