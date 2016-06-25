//Parser Test
//==========

package cas.cs4tb3.mellowd;

//Declare all of the required imports. Gson is the test configuration parser
//that will be used in combination with some of the java.io classes. This class
//will also need some antlr v4 runtime classes for passing the test data into
//the lexer and parser. We also need to import the test classes to run the test
//via junit.
import cas.cs4tb3.mellowd.parser.MellowDLexer;
import cas.cs4tb3.mellowd.parser.MellowDParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

//This test will use the Parameterized runner. This is a JUnit runner that executes
//all tests in this class for each test input data returned by [loadTests()](#test-data-loader).
@RunWith(Parameterized.class)
public class ParserTest {
    //This AtomicInteger will track the test number. It is a thread-safe integer that
    //we can keep increasing for each test to give each test an id.
    private static final AtomicInteger TEST_NUM = new AtomicInteger(0);

    //Test Data Loader
    //----------------
    //Read the test data input configuration and build a test instance for each
    //case described in the file. Each `Object[]` is a set of constructor arguments
    //that will be used to create a new `ParserTest` instance who's [parseTest()](#test-case)
    //will be invoked.
    @Parameterized.Parameters(name = "{index}: {1}")
    public static Collection<Object[]> loadTests() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("testdata/ParserTestData.json")));
        Gson gson = new GsonBuilder().create();
        Object[][] data = gson.fromJson(reader, String[][].class);
        reader.close();
        return Arrays.asList(data);
    }

    //Declare some fields specific to each test run. We need to track the test info
    //`inputFileName` and `ruleName`. We also need to hold onto the `inputStream` so
    //we can close it at the end of the test. The `parser` is the instance that the
    //test is being run on and our `errorListener` will track some more detailed
    //information about what is going on during the test.
    private MellowDParser parser;
    private TestErrorListener errorListener;
    private String inputFileName;
    private String ruleName;
    private int amt;
    private InputStream inputStream;

    public ParserTest(String inputFile, String ruleName, String amt) throws IOException {
        //Get the test resource and save a reference to it so it may be closed
        //upon completion of the test.
        this.inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("parsertest" + File.separator + inputFile);

        //Instantiate the lexer and parser. The parser's default error listener
        //needs to be overridden with a custom one to store all error messages.
        MellowDLexer lexer = new MellowDLexer(new ANTLRInputStream(this.inputStream));

        this.parser = new MellowDParser(new CommonTokenStream(lexer));
        this.parser.setBuildParseTree(true);
        this.parser.removeErrorListeners();
        this.errorListener = new TestErrorListener();
        this.parser.addErrorListener(this.errorListener);
        this.inputFileName = inputFile;
        this.ruleName = ruleName;
        this.amt = Integer.parseInt(amt);
    }

    //Test Case
    //---------
    //Try and parse the input file starting at the given start rule.
    @Test
    public void parseTest() throws Exception {
        try {
            Method ruleMethod = parser.getClass().getMethod(ruleName);
            //Print out some information about the test. This includes a marker ID which is simply
            //a number that uniquely identifies a test from this set. It also displays the target
            //starting rule and the name of the input file used.
            System.out.println("Parser Test " + TEST_NUM.getAndIncrement() + ": Rule=" + this.ruleName
                    + " InFile=" + this.inputFileName);
            System.out.print("-----------------------------------------------------------------------------------\n");
            for (int runNum = 0; runNum < this.amt; runNum++) {
                System.out.printf("Run: %d\n\t", runNum);
                ParserRuleContext context = (ParserRuleContext) ruleMethod.invoke(parser);

                //Check for a failed parse.
                if (this.errorListener.encounteredError()) {
                    //Print out more detailed information about the failure.
                    System.out.println("FAILED: Errors encountered while parsing.");
                    for (String error : this.errorListener.getErrors()) {
                        System.out.print("\t\t");
                        System.out.println(error);
                    }
                    fail("Exceptions thrown while parsing. See standard output for more detail.");
                }

                //If we reach this part in the test code the test has passed so we will
                //print out some information about the data parsed. This includes the matched rule,
                //the starting token, the final token and the parse tree.
                System.out.println(context.toInfoString(this.parser));
                System.out.println();
            }
            //No matter how the test goes we must close the input stream.
        } finally {
            this.inputStream.close();
        }
    }
}
