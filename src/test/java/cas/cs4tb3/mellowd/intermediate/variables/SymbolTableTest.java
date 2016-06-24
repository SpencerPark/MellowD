package cas.cs4tb3.mellowd.intermediate.variables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
}