package org.mellowd.intermediate.variables;

public class NullMemory implements Memory {
    private static NullMemory instance;

    public static NullMemory getInstance() {
        if (instance == null) instance = new NullMemory();
        return instance;
    }

    @Override
    public void set(String name, Object value) { }

    @Override
    public void define(String name, Object value) { }

    @Override
    public Object get(String name) {
        return null;
    }

    @Override
    public boolean isDefined(String name) {
        return false;
    }

    @Override
    public void setNamespace(String name, Memory namespace) { }

    @Override
    public Memory lookupOrCreateNamespace(String name) {
        return NullMemory.getInstance();
    }

    @Override
    public Memory lookupNamespace(String name) {
        return null;
    }

    @Override
    public int countReferences() {
        return 0;
    }

    @Override
    public String dump() {
        return "MEMORY DUMP> References: 0";
    }
}
