package org.mellowd.intermediate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class QualifiedName {
    public static QualifiedName of(String[] qualifier, String name) {
        return Qualifier.of(qualifier).qualify(name);
    }

    public static QualifiedName of(List<String> qualifier, String name) {
        return Qualifier.of(qualifier).qualify(name);
    }

    public static QualifiedName of(Qualifier qualifier, String name) {
        return new QualifiedName(qualifier, name);
    }

    public static QualifiedName ofUnqualified(String name) {
        return new QualifiedName(Qualifier.EMPTY, name);
    }

    public static QualifiedName fromString(String name) {
        String[] split = name.split("\\.");
        if (split.length == 1)
            return new QualifiedName(Qualifier.EMPTY, name);

        return Qualifier.of(Arrays.copyOf(split, split.length - 1))
                .qualify(split[split.length - 1]);
    }

    private final Qualifier qualifier;
    private final String name;

    private QualifiedName(Qualifier qualifier, String name) {
        this.qualifier = qualifier;
        this.name = name;
    }

    public boolean isUnqualified() {
        return this.qualifier.isEmpty();
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public String getName() {
        return name;
    }

    public QualifiedName append(QualifiedName other) {
        String[] qualifier = Arrays.copyOf(this.qualifier.getPath(), this.qualifier.getPath().length + 1 + other.qualifier.getPath().length);
        qualifier[this.qualifier.getPath().length] = this.name;
        System.arraycopy(other.qualifier.getPath(), 0, qualifier, qualifier.length, other.qualifier.getPath().length);

        return QualifiedName.of(qualifier, other.name);
    }

    public QualifiedName append(String name) {
        String[] qualifier = Arrays.copyOf(this.qualifier.getPath(), this.qualifier.getPath().length + 1);
        qualifier[this.qualifier.getPath().length] = this.name;

        return QualifiedName.of(qualifier, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QualifiedName that = (QualifiedName) o;
        return Objects.equals(qualifier, that.qualifier) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifier, name);
    }

    @Override
    public String toString() {
        return this.qualifier.toString() + this.name;
    }
}
