package cas.cs4tb3.mellowd.compiler;

import java.io.FileNotFoundException;
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
    public InputStream resolve(String... importPath) throws FileNotFoundException {
        if (importPath == null || importPath.length == 0)
            throw new FileNotFoundException("Cannot resolve an import with no name");

        importPath[importPath.length-1] += extension;

        String path = String.join("/", importPath);
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
