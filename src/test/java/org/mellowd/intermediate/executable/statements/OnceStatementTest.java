package org.mellowd.intermediate.executable.statements;

import org.junit.Test;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.NullOutput;
import org.mellowd.testutil.CompilerTestFrame;
import org.mellowd.testutil.TestEnvironment;

public class OnceStatementTest extends CompilerTestFrame {
    public OnceStatementTest() {
        super(OnceStatementTest.class.getSimpleName());
    }

    @Test
    public void definesConstantOnce() {
        super.init("once { def mel -> [a, b, c] }");

        Statement stmt = super.compiler.visitStmt(super.parser.stmt());

        ExecutionEnvironment env = new TestEnvironment();

        // Executing twice would throw an exception if not in a "once"
        stmt.execute(env, NullOutput.getInstance());
        stmt.execute(env, NullOutput.getInstance());
    }
}