package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

public class RepeatedCodeBlock extends CodeBlock {
    protected final int repetitions;

    public RepeatedCodeBlock(int repetitions) {
        this.repetitions = repetitions;
    }

    @Override
    public List<Statement> getStatements() {
        List<Statement> stmts = new ArrayList<>(super.statements.size() * repetitions);

        for (int i = 0; i < repetitions; i++)
            stmts.addAll(super.statements);

        return stmts;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (int i = 0; i < repetitions; i++) {
            for (Statement stmt : super.statements) {
                stmt.execute(environment, output);
            }
        }
    }
}
