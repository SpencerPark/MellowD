package org.mellowd.intermediate.executable.statements;

import org.junit.Test;
import org.mellowd.compiler.ExecutionEnvironment;
import org.mellowd.intermediate.NullOutput;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.variables.AlreadyDefinedException;
import org.mellowd.primitives.ArticulatedPitch;
import org.mellowd.primitives.Melody;
import org.mellowd.primitives.Pitch;
import org.mellowd.testutil.CompilerTestFrame;
import org.mellowd.testutil.TestEnvironment;

import static org.junit.Assert.assertEquals;

public class OnceStatementTest extends CompilerTestFrame {
    public OnceStatementTest() {
        super(OnceStatementTest.class.getSimpleName());
    }

    @Test
    public void definesConstantOnce() {
        super.init("once { def mel -> [a] }");

        Statement stmt = super.compiler.visitStmt(super.parser.stmt());
        super.errorListener.assertNoErrors();

        ExecutionEnvironment env = new TestEnvironment();

        // Executing twice would throw an exception if not in a "once"
        stmt.execute(env, NullOutput.getInstance());
        stmt.execute(env, NullOutput.getInstance());

        assertEquals(new Melody(new ArticulatedPitch(Pitch.A)),
                env.getMemory().get(QualifiedName.ofUnqualified("mel"), Melody.class));
    }

    @Test
    public void definesBracesNotNecessaryForSingleStmt() {
        super.init("once def mel -> [b]");

        Statement stmt = super.compiler.visitStmt(super.parser.stmt());
        super.errorListener.assertNoErrors();

        ExecutionEnvironment env = new TestEnvironment();

        // Executing twice would throw an exception if not in a "once"
        stmt.execute(env, NullOutput.getInstance());
        stmt.execute(env, NullOutput.getInstance());

        assertEquals(new Melody(new ArticulatedPitch(Pitch.B)),
                env.getMemory().get(QualifiedName.ofUnqualified("mel"), Melody.class));
    }

    @Test(expected = AlreadyDefinedException.class)
    public void doubleDefineThrows() {
        super.init("def mel -> [c]");

        Statement stmt = super.compiler.visitStmt(super.parser.stmt());
        super.errorListener.assertNoErrors();

        ExecutionEnvironment env = new TestEnvironment();

        // Executing twice would throw an exception if not in a "once"
        stmt.execute(env, NullOutput.getInstance());
        stmt.execute(env, NullOutput.getInstance());
    }
}