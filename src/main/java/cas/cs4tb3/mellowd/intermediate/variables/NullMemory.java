package cas.cs4tb3.mellowd.intermediate.variables;

public class NullMemory implements Memory {
    private static NullMemory instance;

    public static NullMemory getInstance() {
        if (instance == null) instance = new NullMemory();
        return instance;
    }

    @Override
    public void set(String identifier, Object value) { }

    @Override
    public void define(String identifier, Object value) { }

    @Override
    public <T> T get(String identifier, Class<T> type) {
        return null;
    }

    @Override
    public Object get(String identifier) {
        return null;
    }

    @Override
    public Class<?> getType(String identifier) {
        return null;
    }

    @Override
    public boolean identifierTypeIs(String identifier, Class<?> type) {
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
