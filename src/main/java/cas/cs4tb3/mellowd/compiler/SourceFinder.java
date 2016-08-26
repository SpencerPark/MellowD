package cas.cs4tb3.mellowd.compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourceFinder {
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("^(?<name>.*?)\\.(?<ext>.*?)$");

    private final File root;
    private final String extension;

    public SourceFinder(File root, String extension) {
        this.root = root;
        this.extension = extension;
    }

    public File resolve(String... importPath) throws FileNotFoundException {
        StringBuilder path = new StringBuilder();

        for (int i = 0; i < importPath.length - 1; i++) {
            path.append(importPath[i]);
            path.append(File.separator);
        }

        String name = importPath[importPath.length - 1];
        if (!name.endsWith(extension))
            name = name + extension;
        path.append(name);

        File source = new File(root, path.toString());
        if (source.exists()) {
            if (source.isFile())
                return source;

            return throwFileException("Import path %s is a directory. It contains the following source files %s.",
                    getPackage(importPath), Arrays.toString(listSourceNamesIn(source)));
        } else {
            String[] possible = lookForFilesWithWrongExtension(getPackage(importPath), name);
            if (possible == null)
                return throwFileException("The file named '%s' doesn't exist in the '%s' package.",
                        name, getPackage(importPath));
            Matcher m = FILE_EXTENSION_PATTERN.matcher(name);
            if (m.matches()) {
                return throwFileException("The file named '%s' doesn't exist in the '%s' package. Do you need to rename one of %s?",
                        name, getPackage(importPath), Arrays.toString(possible));
            } else {
                //The wrong extension was given
                return throwFileException("The import '%s' doesn't have the '%s' extension.",
                        importPath, extension);
            }
        }
    }

    public File[] listSourcesIn(File directory) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("\"" + directory.getAbsolutePath() + "\" is not a directory.");

        return directory.listFiles((dir, name) -> name.endsWith(extension));
    }

    public String[] listSourceNamesIn(File directory) {
        if (!directory.isDirectory())
            throw new IllegalArgumentException("\"" + directory.getAbsolutePath() + "\" is not a directory.");

        return directory.list((dir, name) -> name.endsWith(extension));
    }

    private File throwFileException(String format, Object... args) throws FileNotFoundException {
        throw new FileNotFoundException(String.format(format, args));
    }

    private String getPackage(String[] importSplit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < importSplit.length - 1; i++) {
            sb.append(importSplit[i]);
            if (i != importSplit.length - 2)
                sb.append(File.separator);
        }
        return sb.toString();
    }

    private String[] lookForFilesWithWrongExtension(String packagePath, String fullName) {
        File directoryToSearch = new File(root, packagePath);
        if (!directoryToSearch.exists() || !directoryToSearch.isDirectory())
            return null;

        Matcher m = FILE_EXTENSION_PATTERN.matcher(fullName);
        String name = m.matches() ? m.group("name") : fullName;

        return directoryToSearch.list((dir, fName) -> {
            Matcher matcher = FILE_EXTENSION_PATTERN.matcher(fName);
            return matcher.matches() && matcher.group("name").equals(name);
        });
    }
}
