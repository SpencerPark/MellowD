//Symbol Table
//============

package org.mellowd.intermediate.variables;

import org.mellowd.compiler.PathMap;
import org.mellowd.intermediate.QualifiedName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// A symbol table holds references. It is simply a Map at its core but it provides type
// and exception handling functionality. The `@SuppressWarnings("unchecked")` stops the compiler
// from incorrectly warning about unchecked casts as the check is done via reflection and the
// compiler isn't convinced that it has been checked.
public class SymbolTable implements Memory {
    private final Memory parentScope;
    private final PathMap<Map<String, Object>> data;
    private final Set<QualifiedName> finalNames;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(Memory parentScope) {
        this.parentScope = parentScope;
        this.data = new PathMap<>(new HashMap<>());
        this.finalNames = new HashSet<>();
    }

    private Map<String, Object> getQualifiedSegment(String... qualifier) {
        return this.data.putIfAbsent(HashMap::new, qualifier);
    }

    // `set` is the only data input method for this class. It adds a
    // new mapping for the `identifier` to the `value`. It will overwrite an existing
    // data.
    @Override
    public void set(QualifiedName identifier, Object value) {
        if (this.finalNames.contains(identifier))
            throw new AlreadyDefinedException("Cannot set value for constant value " + identifier);

        this.getQualifiedSegment(identifier.getQualifier())
                .put(identifier.getName(), value);
    }

    @Override
    public void define(QualifiedName identifier, Object value) {
        if (this.finalNames.contains(identifier))
            throw new AlreadyDefinedException("Constant value " + identifier + " already defined");

        Map<String, Object> segment = this.getQualifiedSegment(identifier.getQualifier());
        if (segment.containsKey(identifier.getName()))
            throw new AlreadyDefinedException("Identifier " + identifier + " already exists and cannot be made into a constant");

        segment.put(identifier.getName(), value);
        this.finalNames.add(identifier);
    }

    @Override
    public boolean isDefined(QualifiedName identifier) {
        Map<String, Object> segment = this.data.get(identifier.getQualifier());
        if (segment == null)
            return this.parentScope != null && this.parentScope.isDefined(identifier);

        return segment.get(identifier.getName()) != null
                || (this.parentScope != null && this.parentScope.isDefined(identifier));
    }

    // `get` is the based data output method for this class. It takes
    // the name of the `identifier` to lookup and the expected type. If the value
    // does not exist this method simply returns null. If the value exists but is the wrong type
    // this method will treat the identifier as non-existent and return null. Otherwise the
    // value is returned.
    @Override
    public <T> T get(QualifiedName identifier, Class<T> type) {
        Object value = this.get(identifier);

        return type.isInstance(value) ? (T) value : null;
    }

    @Override
    public Object get(QualifiedName identifier) {
        Map<String, Object> segment = this.data.get(identifier.getQualifier());

        Object value = null;
        if (segment != null)
            value = segment.get(identifier.getName());

        if (value == null && this.parentScope != null)
            return this.parentScope.get(identifier);

        if (value instanceof DelayedResolution) {
            // We have a variable that is dependent on other data. We will try to resolve it now
            value = ((DelayedResolution) value).resolve(this);
            // If the resolution is successful we will store the resolved value
            if (value != null)
                segment.put(identifier.getName(), value);
        }

        return value;
    }

    // `getOrThrow` is similar in function to `get` but instead
    // of returning null, the appropriate exception will be thrown.
    public <T> T getOrThrow(QualifiedName identifier, Class<T> type) {
        Object value = this.get(identifier);

        // If the value is still null then there is nothing defined so throw an
        // [UndefinedReferenceException](UndefinedReferenceException.html).
        if (value == null)
            throw new UndefinedReferenceException(identifier);

        // If the type of the value is incorrect then something is defined but it is the wrong type
        // so throw an [IncorrectTypeException](IncorrectTypeException.html).
        if (!type.isInstance(value))
            throw new IncorrectTypeException(identifier, value.getClass(), type);

        // Otherwise all is fine so we can safely cast and return the value
        return (T) value;
    }

    // `getType` is a utility method that will return the type of the data. This
    // will return null if the identifier is not defined.
    @Override
    public Class<?> getType(QualifiedName identifier) {
        Object value = this.get(identifier);
        return value == null ? null : value.getClass();
    }

    // `identifierTypeIs` preforms a type check on the identifier. It
    // will return true if the identifier is defined and points to an object
    // that can safely be cast to `type`
    @Override
    public boolean identifierTypeIs(QualifiedName identifier, Class<?> type) {
        Object value = this.get(identifier);
        return value != null && type.isAssignableFrom(value.getClass());
    }

    // `checkType` is the equivalent of `identifierTypeIs` throwing an exception
    // if the type is defined and incorrect (not able to be cast to `type`).
    public void checkType(QualifiedName identifier, Class<?> type) {
        Object value = this.get(identifier);
        if (value != null && !type.isAssignableFrom(value.getClass()))
            throw new IncorrectTypeException(identifier, value.getClass(), type);
    }

    // `checkExists` is a utility method that throws an exception if the identifier that
    // the `token` points to is not defined.
    public void checkExists(QualifiedName identifier) {
        Object value = this.get(identifier);
        if (value == null)
            throw new UndefinedReferenceException(identifier);
    }

    @Override
    public int countReferences() {
        return this.data.reduce(Map::size, (a, b) -> a + b)
                + (this.parentScope != null ? this.parentScope.countReferences() : 0);
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
        this.data.forEach(segment ->
                segment.forEach((id, val) -> {
                    for (int i = 0; i < indentation; i++)
                        sb.append('\t');
                    sb.append(id)
                            .append("->")
                            .append(val == null ? null : val.toString())
                            .append('\n');
                })
        );
        if (this.parentScope != null) {
            if (this.parentScope instanceof SymbolTable) {
                ((SymbolTable) this.parentScope).dumpInternal(indentation + 1, sb);
            } else {
                sb.append(this.parentScope.dump());
            }
        }
    }
}
