package org.mellowd.io;

import java.io.InputStream;

/**
 * A {@link SourceFinder} implementation that looks for resources via
 * the calling {@link Thread}s class loader.
 */
public class ResourceSourceFinder implements SourceFinder {
    private final String extension;

    public ResourceSourceFinder(String extension) {
        this.extension = extension;
    }

    @Override
    public InputStream resolve(String[] importPath) throws SourceResolutionException {
        if (importPath == null || importPath.length == 0)
            throw new SourceResolutionException(importPath, "Cannot resolve an import with no name");

        importPath[importPath.length - 1] += extension;

        String path = String.join("/", importPath);
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (resource != null) return resource;
        throw new SourceResolutionException(importPath, "Cannot find resource \"" + String.join(".", importPath) + "\"");
    }
}
