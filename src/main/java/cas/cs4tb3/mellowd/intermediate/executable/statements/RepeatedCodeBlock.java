package cas.cs4tb3.mellowd.intermediate.executable.statements;

import cas.cs4tb3.mellowd.intermediate.Output;
import cas.cs4tb3.mellowd.intermediate.executable.expressions.Expression;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.parser.ExecutionEnvironment;

public class RepeatedCodeBlock extends CodeBlock {
    public static final String IMPLICIT_LOOP_COUNTER_ID = "it";

    protected final Expression<Number> repetitions;

    public RepeatedCodeBlock(Expression<Number> repetitions) {
        super();
        this.repetitions = repetitions;
    }

    @Override
    public void execute(ExecutionEnvironment environment, Output output) {
        int repetitions = this.repetitions.evaluate(environment).intValue();
        Memory memory = environment.getMemory();
        Object old = memory.get(IMPLICIT_LOOP_COUNTER_ID);

        for (int i = 0; i < repetitions; i++) {
            memory.set(IMPLICIT_LOOP_COUNTER_ID, i);
            for (Statement stmt : super.statements) {
                stmt.execute(environment, output);
            }
        }

        memory.set(IMPLICIT_LOOP_COUNTER_ID, old);
    }
}
