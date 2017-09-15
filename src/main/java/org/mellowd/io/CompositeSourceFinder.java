package org.mellowd.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link SourceFinder} implementation looks for sources in multiple
 * locations.
 */
public class CompositeSourceFinder implements SourceFinder {
    private final SourceFinder[] finders;

    /**
     * The ordering of the {@code finders} is important as it is the order
     * that they will be invoked to look for source files. In the event of duplicate
     * source files, the one found by the {@code finder} with the lowest index is returned.
     * @param finders the delegates to use for looking for source files
     */
    public CompositeSourceFinder(SourceFinder... finders) {
        this.finders = finders;
    }

    @Override
    public InputStream resolve(String... importPath) throws SourceResolutionException {
        List<String> errors = null;
        for (SourceFinder finder : finders) {
            try {
                return finder.resolve(importPath);
            } catch (SourceResolutionException e) {
                if (errors == null) errors = new ArrayList<>();
                for (String err : e) errors.add(err);
            }
        }

        throw new SourceResolutionException(importPath, errors);
    }
}
