package org.mellowd.intermediate.executable.expressions;

import org.mellowd.primitives.Pitch;
import org.mellowd.testutil.DummyEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class BooleanExpressionTest {

    @Test
    public void shortCircuitAND() throws Exception {
        BitSet expressionsExecuted = new BitSet();
        List<Expression<Boolean>> exprs = Arrays.asList(
                (env) -> { expressionsExecuted.set(0); return true; },
                (env) -> { expressionsExecuted.set(1); return false; },
                (env) -> { expressionsExecuted.set(2); return true; },
                (env) -> { expressionsExecuted.set(3); return false; }
        );

        BooleanANDChain and = new BooleanANDChain(exprs);

        and.evaluate(DummyEnvironment.getInstance());

        String msg = "%s expression %s executed in ( true & false & true & false )";
        assertTrue(String.format(msg, "First", "not"),
                expressionsExecuted.get(0));
        assertTrue(String.format(msg, "Second", "not"),
                expressionsExecuted.get(1));
        assertFalse(String.format(msg, "Third", "was"),
                expressionsExecuted.get(2));
        assertFalse(String.format(msg, "Fourth", "was"),
                expressionsExecuted.get(3));
    }

    @Test
    public void shortCircuitOR() throws Exception {
        BitSet expressionsExecuted = new BitSet();
        List<Expression<Boolean>> exprs = Arrays.asList(
                (env) -> { expressionsExecuted.set(0); return false; },
                (env) -> { expressionsExecuted.set(1); return true; },
                (env) -> { expressionsExecuted.set(2); return false; },
                (env) -> { expressionsExecuted.set(3); return true; }
        );

        BooleanORChain or = new BooleanORChain(exprs);

        or.evaluate(DummyEnvironment.getInstance());

        String msg = "%s expression %s executed in ( false | true | false | true )";
        assertTrue(String.format(msg, "First", "not"),
                expressionsExecuted.get(0));
        assertTrue(String.format(msg, "Second", "not"),
                expressionsExecuted.get(1));
        assertFalse(String.format(msg, "Third", "was"),
                expressionsExecuted.get(2));
        assertFalse(String.format(msg, "Fourth", "was"),
                expressionsExecuted.get(3));
    }

    @Test
    public void boolEvalBooleanTrue() throws Exception {
        Expression<Boolean> evalTrue = new BooleanEvaluationExpression((env) -> true);
        assertTrue(evalTrue.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanTrueBoxed() throws Exception {
        Expression<Boolean> evalTrueBox = new BooleanEvaluationExpression((env) -> Boolean.TRUE);
        assertTrue(evalTrueBox.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanFalse() throws Exception {
        Expression<Boolean> evalFalse = new BooleanEvaluationExpression((env) -> false);
        assertFalse(evalFalse.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanFalseBoxed() throws Exception {
        Expression<Boolean> evalFalseBox = new BooleanEvaluationExpression((env) -> Boolean.FALSE);
        assertFalse(evalFalseBox.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalNumberLargerThan0() throws Exception {
        Expression<Boolean> evalInt = new BooleanEvaluationExpression((env) -> 10);
        Expression<Boolean> evalDouble = new BooleanEvaluationExpression((env) -> 30.0);
        Expression<Boolean> evalByte = new BooleanEvaluationExpression((env) -> (byte) 1);

        assertTrue("boolEval(10)",
                evalInt.evaluate(DummyEnvironment.getInstance()));
        assertTrue("boolEval(30.0)",
                evalDouble.evaluate(DummyEnvironment.getInstance()));
        assertTrue("boolEval((byte) 1)",
                evalByte.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalNumberEq0() throws Exception {
        Expression<Boolean> evalInt = new BooleanEvaluationExpression((env) -> 0);
        Expression<Boolean> evalDouble = new BooleanEvaluationExpression((env) -> 0.0);
        Expression<Boolean> evalByte = new BooleanEvaluationExpression((env) -> (byte) 0);

        assertFalse("boolEval(0)",
                evalInt.evaluate(DummyEnvironment.getInstance()));
        assertFalse("boolEval(0.0)",
                evalDouble.evaluate(DummyEnvironment.getInstance()));
        assertFalse("boolEval((byte) 0)",
                evalByte.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalPitchRest() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression((env) -> Pitch.REST);

        assertFalse("boolEval(Pitch.REST)",
                eval.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalPitch() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression((env) -> Pitch.A);

        assertTrue("boolEval(Pitch.A)",
                eval.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalNull() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression((env) -> null);

        assertFalse("boolEval(null)",
                eval.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalNonNullObject() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression((env) -> new Object());

        assertTrue("boolEval(new Object())",
                eval.evaluate(DummyEnvironment.getInstance()));
    }


}