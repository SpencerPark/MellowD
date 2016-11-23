package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

import java.util.LinkedList;
import java.util.List;

public class CodeBlock implements Statement {
    protected final List<Statement> statements;

    public CodeBlock() {
        this.statements = new LinkedList<>();
    }

    public void add(Statement statement) {
        this.statements.add(statement);
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        for (Statement stmt : statements) {
            stmt.execute(environment, output);
        }
    }
}
