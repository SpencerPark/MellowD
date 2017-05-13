package cas.cs4tb3.mellowd.intermediate.variables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class SymbolTableTest {
    private SymbolTable superTable;
    private Memory table;

    @Before
    public void setUp() throws Exception {
        this.superTable = new SymbolTable();
        this.table = new SymbolTable(superTable);
    }

    @After
    public void tearDown() throws Exception {
        this.superTable = null;
        this.table = null;
    }

    @Test
    public void testAddDeclaration() throws Exception {
        this.table.set("Sample", 10);
        assertEquals(10, this.table.get("Sample"));
    }

    @Test
    public void testAddSuperDeclaration() throws Exception {
        this.superTable.set("Sample", 10);
        assertEquals(10, this.table.get("Sample"));
    }

    @Test
    public void testAddDeclarationPrecedence() throws Exception {
        this.superTable.set("Sample", 10);
        this.table.set("Sample", 20);

        assertEquals(10, this.superTable.get("Sample"));
        assertEquals(20, this.table.get("Sample"));
    }

    @Test
    public void testAddDeclarationPrecedence2() throws Exception {
        this.superTable.set("Sample", 10);
        assertEquals(10, this.table.get("Sample"));

        this.table.set("Sample", 20);
        assertEquals(20, this.table.get("Sample"));
    }

    @Test
    public void testAddDefinition() throws Exception {
        this.table.define("Constant", 10);
        assertEquals(10, this.table.get("Constant"));
    }

    @Test
    public void testAddSuperDefinition() throws Exception {
        this.superTable.define("Constant", 10);
        assertEquals(10, this.table.get("Constant"));
    }

    @Test
    public void testAddDefinitionPrecedence() throws Exception {
        this.superTable.define("Constant", 10);
        this.table.define("Constant", 20);

        assertEquals(10, this.superTable.get("Constant"));
        assertEquals(20, this.table.get("Constant"));
    }

    @Test
    public void testAddDefinitionPrecedence2() throws Exception {
        this.superTable.define("Constant", 10);
        assertEquals(10, this.table.get("Constant"));

        this.table.define("Constant", 20);
        assertEquals(20, this.table.get("Constant"));
    }

    @Test
    public void testCantRedefineDefinition() throws Exception {
        this.table.define("Constant", 10);

        try {
            this.table.define("Constant", 20);
            fail("No exception thrown when trying to redefine a constant");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but constant value still changed",
                    10, this.table.get("Constant"));
        }
    }

    @Test
    public void testCantSetOverDefinition() throws Exception {
        this.table.define("Constant", 10);

        try {
            this.table.set("Constant", 20);
            fail("No exception thrown when trying to set a value to a constant");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but constant value still changed",
                    10, this.table.get("Constant"));
        }
    }

    @Test
    public void testCantDefineAlreadySet() throws Exception {
        this.table.set("Sample", 10);

        try {
            this.table.define("Sample", 20);
            fail("No exception thrown when trying to define a constant over a value");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but value still changed",
                    10, this.table.get("Sample"));
        }
    }

    @Test
    public void testDelayedResolution() throws Exception {
        this.table.set("Sample1",
                (DelayedResolution) mem -> mem.get("Sample2", Integer.class) + 10);
        this.table.set("Sample2", 20);

        assertEquals(30, this.table.get("Sample1"));
    }

    @Test
    public void testDelayedResolutionDefinition() throws Exception {
        this.table.define("Sample1",
                (DelayedResolution) mem -> mem.get("Sample2", Integer.class) + 10);
        this.table.define("Sample2", 20);

        assertEquals(30, this.table.get("Sample1"));
    }
}