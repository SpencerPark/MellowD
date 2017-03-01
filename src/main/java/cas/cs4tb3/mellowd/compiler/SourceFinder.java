package cas.cs4tb3.mellowd.compiler;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface SourceFinder {

    /**
     * Resolve the source file described by the {@code importPath}.
     * @param importPath the import path split at the dots or directory
     * @return the source code as a stream
     * @throws FileNotFoundException if the path cannot be resolved
     */
    InputStream resolve(String... importPath) throws FileNotFoundException;
}
