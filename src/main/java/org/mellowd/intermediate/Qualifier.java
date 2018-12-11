package org.mellowd.intermediate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Qualifier {
    public static final Qualifier EMPTY = Qualifier.of(new String[0]);

    public static Qualifier of(String[] path) {
        return new Qualifier(path);
    }

    public static Qualifier of(List<String> path) {
        return new Qualifier(path.toArray(new String[0]));
    }

    public static Qualifier fromString(String name) {
        return new Qualifier(name.split("\\."));
    }

    private final String[] path;

    private Qualifier(String[] path) {
        this.path = path;
    }

    public boolean isEmpty() {
        return this.path.length == 0;
    }

    public String[] getPath() {
        return this.path;
    }

    public QualifiedName qualify(String name) {
        return QualifiedName.of(this, name);
    }

    public QualifiedName qualify(QualifiedName name) {
        return this.append(name.getQualifier()).qualify(name.getName());
    }

    public Qualifier append(String... qualifier) {
        String[] path = Arrays.copyOf(this.path, this.path.length + qualifier.length);
        System.arraycopy(qualifier, 0, path, this.path.length, qualifier.length);
        return new Qualifier(path);
    }

    public Qualifier append(Qualifier qualifier) {
        return this.append(qualifier.path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Qualifier qualifier = (Qualifier) o;
        return Arrays.equals(path, qualifier.path);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    @Override
    public String toString() {
        return this.isEmpty()
                ? ""
                : Arrays.stream(this.path)
                        .collect(Collectors.joining(".", "", "."));
    }
}
