package org.mellowd.intermediate.variables;

import org.mellowd.intermediate.QualifiedName;

public class NullMemory implements Memory {
    private static NullMemory instance;

    public static NullMemory getInstance() {
        if (instance == null) instance = new NullMemory();
        return instance;
    }

    @Override
    public void set(QualifiedName identifier, Object value) { }

    @Override
    public void define(QualifiedName identifier, Object value) { }

    @Override
    public <T> T get(QualifiedName identifier, Class<T> type) {
        return null;
    }

    @Override
    public Object get(QualifiedName identifier) {
        return null;
    }

    @Override
    public boolean isDefined(QualifiedName identifier) {
        return false;
    }

    @Override
    public Class<?> getType(QualifiedName identifier) {
        return null;
    }

    @Override
    public boolean identifierTypeIs(QualifiedName identifier, Class<?> type) {
        return false;
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
