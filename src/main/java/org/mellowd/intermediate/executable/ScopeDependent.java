package org.mellowd.intermediate.executable;

import org.mellowd.intermediate.QualifiedName;

import java.util.Set;

public interface ScopeDependent {
    public Set<QualifiedName> getFreeVariables();
}
