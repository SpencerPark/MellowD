//Symbol Table
//============

package cas.cs4tb3.mellowd.intermediate.variables;

import java.util.HashMap;
import java.util.Map;

//A symbol table holds references. It is simply a Map at its core but it provides type
//and exception handling functionality. The `@SuppressWarnings("unchecked")` stops the compiler
//from incorrectly warning about unchecked casts as the check is done via reflection and the
//compiler isn't convinced that it has been checked.
@SuppressWarnings("unchecked")
public class SymbolTable implements Memory {
    private final Memory superTable;
    //The `declarations` are the actually `name -> value` mappings.
    private final Map<String, Object> declarations;

    public SymbolTable() {
        this.superTable = null;
        declarations = new HashMap<>();
    }

    public SymbolTable(Memory superTable) {
        this.superTable = superTable;
        declarations = new HashMap<>();
    }

    //`set` is the only data input method for this class. It adds a
    //new mapping for the `identifier` to the `value`. It will overwrite an existing
    //data and will return true if it does so.
    @Override
    public void set(String identifier, Object value) {
        this.declarations.put(identifier, value);
    }

    //`get` is the based data output method for this class. It takes
    //the name of the `identifier` to lookup and the expected type. If the value
    //does not exist this method simply returns null. If the value exists but is the wrong type
    //this method will treat the identifier as non-existent and return null. Otherwise the
    //value is returned.
    @Override
    public <T> T get(String identifier, Class<T> type) {
        Object value = get(identifier);
        if (value == null) return superTable == null ? null : superTable.get(identifier, type);
        return type.isInstance(value) ? (T) value : null;
    }

    @Override
    public Object get(String identifier) {
        Object value = declarations.get(identifier);

        if (value == null && superTable != null)
            return superTable.get(identifier);

        if (value instanceof DelayedResolution) {
            //We have a variable that is dependent on other data. We will try to resolve it now
            value = ((DelayedResolution) value).resolve(this);
            //If the resolution is successful we will store the resolved value
            if (value != null) {
                this.set(identifier, value);
            }
        } else if (value instanceof ContextDependent) {
            //We have a variable that has a different value depending on the context. We will
            //resolve it now but leave the context dependent instance in the table.
            value = ((ContextDependent) value).resolve(this);
        }

        return value;
    }

    //`getOrThrow` is similar in function to `get` but instead
    //of returning null, the appropriate exception will be thrown.
    @Override
    public <T> T getOrThrow(String identifier, Class<T> type) {
        Object value = get(identifier);

        //If the value is still null then there is nothing defined so throw an
        //[UndefinedReferenceException](UndefinedReferenceException.html).
        if (value == null)
            throw new UndefinedReferenceException(identifier);

        //If the type of the value is incorrect then something is defined but it is the wrong type
        //so throw an [IncorrectTypeException](IncorrectTypeException.html).
        if (!type.isInstance(value))
            throw new IncorrectTypeException(identifier, value.getClass(), type);

        //Otherwise all is fine so we can safely cast and return the value
        return (T) value;
    }

    //`getType` is a utility method that will return the type of the data. This
    //will return null if the identifier is not defined.
    @Override
    public Class<?> getType(String identifier) {
        Object value = get(identifier);
        return value == null ? null : value.getClass();
    }

    //`identifierTypeIs` preforms a type check on the identifier. It
    //will return true if the identifier is defined and points to an object
    //that can safely be cast to `type`
    @Override
    public boolean identifierTypeIs(String identifier, Class<?> type) {
        Object value = get(identifier);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    //`checkType` is the equivalent of `identifierTypeIs` throwing an exception
    //if the type is defined and incorrect (not able to be cast to `type`).
    @Override
    public void checkType(String identifier, Class<?> type) {
        Object value = get(identifier);
        if (value != null && !type.isAssignableFrom(value.getClass()))
            throw new IncorrectTypeException(identifier, value.getClass(), type);
    }

    //`checkExists` is a utility method that throws an exception if the identifier that
    //the `token` points to is not defined.
    @Override
    public void checkExists(String identifier) {
        Object value = get(identifier);
        if (value == null)
            throw new UndefinedReferenceException(identifier);
    }

    @Override
    public int countReferences() {
        return this.declarations.size() + ( this.superTable != null ? this.superTable.countReferences() : 0 );
    }

    @Override
    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("MEMORY DUMP> References: ").append(this.countReferences());
        sb.append('\n');
        dumpInternal(0, sb);
        return sb.toString();
    }

    private void dumpInternal(int indentation, StringBuilder sb) {
        this.declarations.forEach((id, val) -> {
            for (int i = 0; i < indentation; i++)
                sb.append('\t');
            sb.append(id)
                    .append("->")
                    .append(val == null ? null : val.toString())
                    .append('\n');
        });
        if (superTable != null) {
            if (superTable instanceof SymbolTable) {
                ((SymbolTable) superTable).dumpInternal(indentation + 1, sb);
            } else {
                sb.append(superTable.dump());
            }
        }
    }
}
