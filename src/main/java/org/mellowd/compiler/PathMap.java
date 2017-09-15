package org.mellowd.compiler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PathMap<T> {
    private final Map<String, PathMap<T>> children;
    private T data;

    public PathMap(T data) {
        this.data = data;
        this.children = new LinkedHashMap<>();
    }

    public T putIfAbsent(Supplier<T> dataCreator, String... path) {
        return putIfAbsentInternal(dataCreator, path, 0);
    }

    private T putIfAbsentInternal(Supplier<T> dataCreator, String[] path, int index) {
        if (index == path.length) {
            if (this.data == null)
                this.data = dataCreator.get();

            return this.data;
        } else {
            //The data needs to be placed in a child node
            PathMap<T> child = children.get(path[index]);
            if (child == null) {
                //Add the child because it doesn't exist yet
                child = new PathMap<>(null);
                children.put(path[0], child);
            }
            //Put the data in the child
            return child.putIfAbsentInternal(dataCreator, path, index + 1);
        }
    }

    public boolean put(T data, String... path) {
        return putInternal(data, path, 0);
    }

    private boolean putInternal(T data, String[] path, int index) {
        if (index == path.length) {
            boolean overwriting = this.data == null;
            //The data needs to be placed in the current node
            this.data = data;
            return overwriting;
        } else {
            //The data needs to be placed in a child node
            PathMap<T> child = children.get(path[index]);
            if (child == null) {
                //Add the child because it doesn't exist yet
                child = new PathMap<>(null);
                children.put(path[0], child);
            }
            //Put the data in the child
            return child.putInternal(data, path, index + 1);
        }
    }

    public T get(String... path) {
        return getInternal(path, 0);
    }

    private T getInternal(String[] path, int index) {
        if (index == path.length) {
            //The path points to this node
            return this.data;
        } else {
            //A lookup in a child node is required
            PathMap<T> child = children.get(path[index]);
            if (child == null) {
                //The child doesn't exist so the data doesn't either
                return null;
            }
            //Ask the child for the data
            return child.getInternal(path, index + 1);
        }
    }
}
