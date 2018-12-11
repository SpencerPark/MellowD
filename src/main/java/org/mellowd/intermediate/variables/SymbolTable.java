package org.mellowd.intermediate.variables;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// A symbol table holds references. It is simply a Map at its core but it provides type
// and exception handling functionality. The `@SuppressWarnings("unchecked")` stops the compiler
// from incorrectly warning about unchecked casts as the check is done via reflection and the
// compiler isn't convinced that it has been checked.
public class SymbolTable implements Memory {
    private final Memory parent;

    private final Map<String, Memory> namespaces;

    private final Map<String, Object> data;
    private final Set<String> finalNames;

    public SymbolTable() {
        this(null);
    }

    public SymbolTable(Memory parent) {
        this.parent = parent;

        this.data = new HashMap<>();
        this.finalNames = new HashSet<>();

        this.namespaces = new HashMap<>();
    }

    @Override
    public void set(String name, Object value) {
        if (this.finalNames.contains(name))
            throw new AlreadyDefinedException("Cannot set value for constant value " + name);
        this.data.put(name, value);
    }

    @Override
    public void define(String name, Object value) {
        if (this.finalNames.contains(name))
            throw new AlreadyDefinedException("Constant value " + name + " already defined");

        if (this.data.containsKey(name))
            throw new AlreadyDefinedException("Identifier " + name + " already exists and cannot be made into a constant");

        this.data.put(name, value);
        this.finalNames.add(name);
    }

    @Override
    public Object get(String name) {
        Object value = this.data.get(name);

        if (value == null && this.parent != null)
            return this.parent.get(name);

        if (value instanceof DelayedResolution) {
            // We have a variable that is dependent on other data. We will try to resolve it now
            value = ((DelayedResolution) value).resolve(this);
            // If the resolution is successful we will store the resolved value
            if (value != null)
                this.data.put(name, value);
        }

        return value;
    }

    @Override
    public boolean isDefined(String name) {
        return this.data.containsKey(name)
                || (this.parent != null && this.parent.isDefined(name));
    }

    @Override
    public void setNamespace(String name, Memory namespace) {
        this.namespaces.put(name, namespace);
    }

    @Override
    public Memory lookupOrCreateNamespace(String name) {
        Memory namespace = this.lookupNamespace(name);

        if (namespace == null) {
            namespace = new SymbolTable();
            this.setNamespace(name, namespace);
        }

        return namespace;
    }

    @Override
    public Memory lookupNamespace(String name) {
        Memory namespace = this.namespaces.get(name);

        return namespace == null && this.parent != null
                ? this.parent.lookupNamespace(name) : namespace;
    }

    @Override
    public int countReferences() {
        return this.data.size()
                + this.namespaces.values().stream().mapToInt(Memory::countReferences).sum()
                + (this.parent != null ? this.parent.countReferences() : 0);
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
        this.data.forEach((id, val) -> {
            for (int i = 0; i < indentation; i++)
                sb.append('\t');
            sb.append(id)
                    .append("->")
                    .append(val == null ? null : val.toString())
                    .append('\n');
        });

        this.namespaces.forEach((name, ns) -> {
            sb.append(name).append(">\n");
            if (ns instanceof SymbolTable) {
                ((SymbolTable) ns).dumpInternal(indentation + 1, sb);
            } else {
                sb.append(ns.dump());
            }
        });

        if (this.parent != null) {
            if (this.parent instanceof SymbolTable) {
                ((SymbolTable) this.parent).dumpInternal(indentation + 1, sb);
            } else {
                sb.append(this.parent.dump());
            }
        }
    }
}
