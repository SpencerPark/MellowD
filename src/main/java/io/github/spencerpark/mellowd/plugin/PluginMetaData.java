package io.github.spencerpark.mellowd.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public class PluginMetaData {
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final String name;
    private final String implementationClass;
    private final String version;
    private final String author;
    private final String description;
    private final String website;

    public PluginMetaData(String name, String implementationClass, String version, String author, String description, String website) {
        List<String> badProps = new LinkedList<>();

        if (name == null)
            badProps.add("'name' is a required property");
        else if (!VALID_NAME_PATTERN.matcher(name).matches())
            badProps.add("'name'=" + name + " is not a valid plugin identifier");
        this.name = name;

        if (implementationClass == null)
            badProps.add("'implementation-class' is a required property");
        this.implementationClass = implementationClass;

        if (version == null)
            badProps.add("'version' is a required property");
        this.version = version;

        this.author = author;
        this.description = description;
        this.website = website;

        if (!badProps.isEmpty()) {
            StringJoiner error = new StringJoiner("\t\n");
            badProps.forEach(error::add);

            throw new IllegalArgumentException("Invalid plugin metadata.\n\t" + error);
        }
    }

    public PluginMetaData(Properties properties) {
        this(properties.getProperty("name", null),
                properties.getProperty("implementation-class", null),
                properties.getProperty("version", null),
                properties.getProperty("author", null),
                properties.getProperty("description", null),
                properties.getProperty("website", null));
    }

    public String getName() {
        return name;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }
}
