package org.mellowd.intermediate;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class QualifiedName {
    private static final String[] EMPTY_QUALIFIER = new String[0];

    public static QualifiedName ofUnqualified(String name) {
        return new QualifiedName(EMPTY_QUALIFIER, name);
    }
    
    private final String[] qualifier;
    private final String name;

    public QualifiedName(String[] qualifier, String name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    public boolean hasQualifier() {
        return this.qualifier.length != 0;
    }

    public String[] getQualifier() {
        return qualifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualifiedName that = (QualifiedName) o;
        return Arrays.equals(qualifier, that.qualifier) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(qualifier);
        return result;
    }

    @Override
    public String toString() {
        return Arrays.stream(this.qualifier)
                .collect(Collectors.joining(".", "", "."))
                + this.name;
    }
}
