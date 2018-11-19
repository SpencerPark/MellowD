package org.mellowd.intermediate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QualifiedName {
    private static final String[] EMPTY_QUALIFIER = new String[0];

    public static QualifiedName of(String[] qualifier, String name) {
        return new QualifiedName(qualifier, name);
    }

    public static QualifiedName of(List<String> qualifier, String name) {
        return new QualifiedName(qualifier.toArray(new String[0]), name);
    }

    public static QualifiedName ofUnqualified(String name) {
        return new QualifiedName(EMPTY_QUALIFIER, name);
    }

    public static QualifiedName fromString(String name) {
        String[] split = name.split("\\.");
        if (split.length == 1)
            return new QualifiedName(EMPTY_QUALIFIER, name);

        return new QualifiedName(Arrays.copyOf(split, split.length - 1), split[split.length - 1]);
    }

    private final String[] qualifier;
    private final String name;

    private QualifiedName(String[] qualifier, String name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    public boolean isUnqualified() {
        return this.qualifier.length == 0;
    }

    public String[] getQualifier() {
        return qualifier;
    }

    public String getName() {
        return name;
    }

    public QualifiedName append(QualifiedName other) {
        String[] qualifier = Arrays.copyOf(this.qualifier, this.qualifier.length + 1 + other.qualifier.length);
        qualifier[this.qualifier.length] = this.name;
        System.arraycopy(other.qualifier, 0, qualifier, qualifier.length, other.qualifier.length);

        return new QualifiedName(qualifier, other.name);
    }

    public QualifiedName append(String name) {
        String[] qualifier = Arrays.copyOf(this.qualifier, this.qualifier.length + 1);
        qualifier[this.qualifier.length] = this.name;

        return new QualifiedName(qualifier, name);
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
