package org.mellowd.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectorySourceFinder implements SourceFinder {
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("^(?<name>.*?)\\.(?<ext>.*?)$");

    private final File root;
    private final String extension;

    public DirectorySourceFinder(File root, String extension) {
        this.root = root;
        this.extension = extension;
    }

    @Override
    public InputStream resolve(String[] importPath) throws SourceResolutionException {
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
            try {
                return new FileInputStream(source);
            } catch (FileNotFoundException e) {
                throw new SourceResolutionException(importPath, String.format("Import path %s is a directory. It contains the following source files %s.",
                        getPackage(importPath), Arrays.toString(listSourceNamesIn(source))));
            }
        } else {
            String[] possible = lookForFilesWithWrongExtension(getPackage(importPath), name);
            if (possible == null)
                throw new SourceResolutionException(importPath, String.format("The file named '%s' doesn't exist in the '%s' package.",
                        name, getPackage(importPath)));
            Matcher m = FILE_EXTENSION_PATTERN.matcher(name);
            if (m.matches()) {
                throw new SourceResolutionException(importPath, String.format("The file named '%s' doesn't exist in the '%s' package. Do you need to rename one of %s?",
                        name, getPackage(importPath), Arrays.toString(possible)));
            } else {
                //The wrong extension was given
                throw new SourceResolutionException(importPath, String.format("The import source '%s' doesn't have the '%s' extension.",
                        String.join(".", importPath), extension));
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
