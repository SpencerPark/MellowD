package org.mellowd.intermediate.variables;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mellowd.intermediate.QualifiedName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class SymbolTableTest {
    private QualifiedName sampleName = QualifiedName.ofUnqualified("Sample");
    private QualifiedName sample1Name = QualifiedName.ofUnqualified("Sample1");
    private QualifiedName sample2Name = QualifiedName.ofUnqualified("Sample2");
    private QualifiedName constantName = QualifiedName.ofUnqualified("Constant");
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
        this.table.set(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));
    }

    @Test
    public void testAddSuperDeclaration() throws Exception {
        this.superTable.set(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));
    }

    @Test
    public void testAddDeclarationPrecedence() throws Exception {
        this.superTable.set(sampleName, 10);
        this.table.set(sampleName, 20);

        assertEquals(10, this.superTable.get(sampleName));
        assertEquals(20, this.table.get(sampleName));
    }

    @Test
    public void testAddDeclarationPrecedence2() throws Exception {
        this.superTable.set(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));

        this.table.set(sampleName, 20);
        assertEquals(20, this.table.get(sampleName));
    }

    @Test
    public void testAddDefinition() throws Exception {
        this.table.define(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));
    }

    @Test
    public void testAddSuperDefinition() throws Exception {
        this.superTable.define(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));
    }

    @Test
    public void testAddDefinitionPrecedence() throws Exception {
        this.superTable.define(sampleName, 10);
        this.table.define(sampleName, 20);

        assertEquals(10, this.superTable.get(sampleName));
        assertEquals(20, this.table.get(sampleName));
    }

    @Test
    public void testAddDefinitionPrecedence2() throws Exception {
        this.superTable.define(sampleName, 10);
        assertEquals(10, this.table.get(sampleName));

        this.table.define(sampleName, 20);
        assertEquals(20, this.table.get(sampleName));
    }

    @Test
    public void testCantRedefineDefinition() throws Exception {
        this.table.define(sampleName, 10);

        try {
            this.table.define(sampleName, 20);
            fail("No exception thrown when trying to redefine a constant");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but constant value still changed",
                    10, this.table.get(sampleName));
        }
    }

    @Test
    public void testCantSetOverDefinition() throws Exception {
        this.table.define(sampleName, 10);

        try {
            this.table.set(sampleName, 20);
            fail("No exception thrown when trying to set a value to a constant");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but constant value still changed",
                    10, this.table.get(sampleName));
        }
    }

    @Test
    public void testCantDefineAlreadySet() throws Exception {
        this.table.set(sampleName, 10);

        try {
            this.table.define(sampleName, 20);
            fail("No exception thrown when trying to define a constant over a value");
        } catch (AlreadyDefinedException e) {
            assertEquals("Exception thrown but value still changed",
                    10, this.table.get(sampleName));
        }
    }

    @Test
    public void testDelayedResolution() throws Exception {
        this.table.set(sample1Name,
                (DelayedResolution) mem -> mem.get(sample2Name, Integer.class) + 10);
        this.table.set(sample2Name, 20);

        assertEquals(30, this.table.get(sample1Name));
    }

    @Test
    public void testDelayedResolutionDefinition() throws Exception {
        this.table.define(sample1Name,
                (DelayedResolution) mem -> mem.get(sample2Name, Integer.class) + 10);
        this.table.define(sample2Name, 20);

        assertEquals(30, this.table.get(sample1Name));
    }

    @Test
    public void testDefineNamespace() {
        Memory memory = this.table.lookupOrCreateNamespace("this");
        memory.set("field1", 20);

        assertEquals(20, this.table.get(QualifiedName.fromString("this.field1")));
    }

    @Test
    public void testDefineNestedNamespace() {
        Memory deep = new SymbolTable();
        deep.set("field1", 30);

        this.table.setNamespace(QualifiedName.fromString("this.field1"), deep);
        this.table.set(QualifiedName.fromString("this.field1"), 20);

        assertEquals(20, this.table.get(QualifiedName.fromString("this.field1")));
        assertEquals(30, this.table.get(QualifiedName.fromString("this.field1.field1")));

        deep.set("field1", 10);
        assertEquals(10, this.table.get(QualifiedName.fromString("this.field1.field1")));
    }

    @Test
    public void testCreateNested() {
        QualifiedName id = QualifiedName.fromString("obj1.obj2.field1");

        this.table.set(id, 10);

        assertEquals(10, this.table.get(id));
    }

    @Test
    public void testNamespacesAndVariablesAreSeparate() {
        QualifiedName deepField = QualifiedName.fromString("obj.field_obj.field");
        QualifiedName shallowField = QualifiedName.fromString("obj.field_obj");

        this.table.set(deepField, 10);
        this.table.set(shallowField, 20);

        assertEquals(10, this.table.get(deepField));
        assertEquals(20, this.table.get(shallowField));
    }

    @Test
    public void testModifyParentNamespace() {
        QualifiedName deepField = QualifiedName.fromString("obj.field_obj.field");
        QualifiedName shallowField = QualifiedName.fromString("obj.field_obj");

        this.superTable.lookupOrCreateNamespace("obj");
        this.table.set(deepField, 10);
        this.table.set(shallowField, 20);

        assertEquals(10, this.table.get(deepField));
        assertEquals(20, this.table.get(shallowField));

        assertEquals(10, this.superTable.get(deepField));
        assertEquals(20, this.superTable.get(shallowField));
    }
}