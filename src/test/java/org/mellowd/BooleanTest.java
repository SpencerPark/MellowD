package org.mellowd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mellowd.intermediate.executable.expressions.Expression;
import org.mellowd.parser.MellowDParser;
import org.mellowd.testutil.CompilerTestFrame;
import org.mellowd.testutil.DummyEnvironment;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class BooleanTest extends CompilerTestFrame {
    private class TestCase {
        String src;
        boolean expected;

        public TestCase(String src, boolean expected) {
            this.src = src;
            this.expected = expected;
        }

        public void print(Boolean result) {
            System.out.printf("%s Expecting: %-5b Actual: %-5b Source: %s\n",
                    expected == result ? "[SUCCESS]" : "[FAILURE]", expected, result, src);
        }
    }

    public BooleanTest() {
        super("BooleanTest");
    }

    private Boolean parseBoolExpr() {
        MellowDParser.DisjunctionContext parsedExpr = parser.disjunction();
        Expression<Boolean> expr = compiler.visitDisjunction(parsedExpr);

        return expr.evaluate(DummyEnvironment.getInstance());
    }

    private void printTest(String classifier) {
        String header = super.takeTestHeader(classifier);
        System.out.printf("----------%s----------\n", header);
    }

    private void runTestCases(TestCase... cases) {
        for (TestCase testCase : cases) {
            super.init(testCase.src);

            Boolean val = parseBoolExpr();

            if (super.errorListener.encounteredError()) {
                System.out.println("[ERROR]   Parse errors: " + super.errorListener.getErrors().size());
                for (String error : super.errorListener.getErrors()) {
                    System.out.println("\t" + error);
                }
                fail("Error while parsing \"" + testCase.src + "\"");
            } else {
                testCase.print(val);
            }

            assertTrue(testCase.src, val == testCase.expected);
        }
    }

    @Test
    public void literalAND() throws Exception {
        printTest("literalAND");

        TestCase[] testCases = new TestCase[]{
                new TestCase("true  and true ", true),
                new TestCase("true  and false", false),
                new TestCase("false and true ", false),
                new TestCase("false and false", false),
        };

        runTestCases(testCases);
    }

    @Test
    public void literalOR() throws Exception {
        printTest("literalOR");

        runTestCases(
                new TestCase("true  or true ", true),
                new TestCase("true  or false", true),
                new TestCase("false or true ", true),
                new TestCase("false or false", false)
        );
    }

    @Test
    public void booleanOpPrecedence() throws Exception {
        printTest("booleanOpPrecedence");

        runTestCases(
                new TestCase("true or true and false", true),
                new TestCase("true or false and false", true)
        );
    }

    @Test
    public void constants() throws Exception {
        printTest("constants");

        TestCase[] testCases = new TestCase[]{
                new TestCase("true ", true),
                new TestCase("false", false),
                new TestCase("on   ", true),
                new TestCase("off  ", false),
        };

        runTestCases(testCases);
    }

    @Test
    public void compareChords() throws Exception {
        printTest("compareChords");

        TestCase[] testCases = new TestCase[]{
                new TestCase("(c, e, g) lt (d, e, g)", true),
                new TestCase("(c, e, g) gt (d, e, g)", false),
                new TestCase("C lt C+1", true),
                new TestCase("B gt A", true),
                new TestCase("(g, d, a) eq (g, d, b)", false),
                new TestCase("(g, d, a) lt (g, d, b)", true),
                new TestCase("(g, d, a) geq (g, d, b)", false),
                new TestCase("(g, d, a) geq (g, d, a, g-100)", false),
        };

        runTestCases(testCases);
    }
}
