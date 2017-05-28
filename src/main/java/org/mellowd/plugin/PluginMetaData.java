package org.mellowd.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * The plugin meta data describes things about a plugin. Most importantly
 * the implementation class.
 * <p>
 * The meta data file should be located at the root of the jar named
 * <pre>&lt;plugin-id&gt;.mellowd-plugin.properties</pre> with the following
 * format.
 * <pre>
 * # Required: the id of the plugin. Same as in the file name
 * name=plugin-id
 * # Required: the fully qualified class name of the class that
 * # implements MellowDPlugin
 * implementation-class=java.package.PluginClass
 * # Required: the version of the plugin
 * version=0.1.0
 * # Required: the version of MellowD that the plugin is compatible with
 * mellowd-version=2.+
 * # Optional: the name of the author of the plugin
 * author="Spencer Park"
 * # Optional: a short description of the plugin
 * description=A fantastic plugin that does fantastic things!
 * # Optional: a website such as a github repository associated with the plugin
 * website=spencerpark.github.io/MellowD
 * </pre>
 */
public class PluginMetaData {
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final String name;
    private final String implementationClass;
    private final String version;
    private final String mellowDVersion;
    private final String author;
    private final String description;
    private final String website;

    public PluginMetaData(String name, String implementationClass, String version, String mellowDVersion, String author, String description, String website) {
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

        if (mellowDVersion == null)
            badProps.add("'mellowd-version' is a required proterty");
        this.mellowDVersion = mellowDVersion;

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
                properties.getProperty("mellowd-version", null),
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

    public String getMellowDVersion() {
        return mellowDVersion;
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
