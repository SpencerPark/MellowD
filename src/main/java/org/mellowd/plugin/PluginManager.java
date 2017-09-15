package org.mellowd.plugin;

import org.mellowd.compiler.MellowD;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PluginManager {
    private static String formatMetaDataFilePath(String name) {
        return "/" + name + ".mellowd-plugin.properties";
    }

    private final Map<String, MellowDPlugin> plugins;

    public PluginManager() {
        this.plugins = new HashMap<>();
    }

    /**
     * Register an already instantiated <b>and {@link MellowDPlugin#onLoad() loaded}</b>
     * plugin with the given name. If an instance is already associated with this
     * name it will be evicted and unloaded.
     *
     * @param name   the name of the plugin
     * @param plugin the plugin to register
     */
    public void registerPlugin(String name, MellowDPlugin plugin) {
        MellowDPlugin oldPlugin = this.plugins.put(name, plugin);
        if (oldPlugin != null)
            oldPlugin.onUnload();
    }

    /**
     * Lookup a plugin by name. The name is case-sensitive. If
     * a plugin doesn't exist with the name, {@code null} is
     * returned.
     * <p>
     * See {@link #getOrLoadPlugin(String)} to optionally load the
     * plugin from the classpath if it is not yet loaded.
     *
     * @param name the name of the plugin to load
     *
     * @return the plugin with the given {@code name} or null if
     * one is not loaded
     */
    public MellowDPlugin getPlugin(String name) {
        return this.plugins.get(name);
    }

    /**
     * Lookup a plugin by name. The name is case-sensitive. If
     * a plugin doesn't exist with the name the manager attempts
     * to load the plugin.
     *
     * @param name the name of the plugin to load
     *
     * @return the plugin with the given {@code name}, never {@code null}
     *
     * @throws PluginLoadException if the plugin could not be loaded
     */
    public MellowDPlugin getOrLoadPlugin(String name) throws PluginLoadException {
        MellowDPlugin plugin = this.plugins.get(name);
        if (plugin != null) return plugin;

        return loadPluginOnClasspath(name);
    }

    /**
     * Invoke the {@link MellowDPlugin#apply(MellowD)} method for all
     * plugins managed by this manager.
     *
     * @param mellowD the {@link MellowD} instance to apply all of the
     *                plugins on.
     */
    public void applyAll(MellowD mellowD) {
        this.plugins.values().forEach(plugin -> plugin.apply(mellowD));
    }

    /**
     * Apply the plugins with the given id's if they are loaded.
     *
     * @param mellowD the {@link MellowD} instance to apply the requested
     *                plugins on.
     * @param plugins the plugins to apply
     */
    public void applyIfLoaded(MellowD mellowD, Iterable<String> plugins) {
        plugins.forEach(pluginId -> {
            MellowDPlugin plugin = getPlugin(pluginId);
            if (plugin != null) plugin.apply(mellowD);
        });
    }

    /**
     * Apply the plugins with the given id's. If they are not loaded, they
     * will try to be loaded
     *
     * @param mellowD the {@link MellowD} instance to apply the requested
     *                plugins on.
     * @param plugins the plugins to apply
     *
     * @throws PluginLoadException if a plugin could not be loaded
     */
    public void applySome(MellowD mellowD, Iterable<String> plugins) throws PluginLoadException {
        for (String pluginId : plugins) {
            MellowDPlugin plugin = getOrLoadPlugin(pluginId);
            if (plugin != null) plugin.apply(mellowD);
        }
    }

    /**
     * Unload all of the plugins from this manager
     */
    public void unloadAll() {
        this.plugins.values().forEach(MellowDPlugin::onUnload);
        this.plugins.clear();
    }

    private MellowDPlugin loadPluginOnClasspath(String name) throws PluginLoadException {
        String path = formatMetaDataFilePath(name);

        InputStream resource = getClass().getResourceAsStream(path);
        if (resource == null)
            throw new PluginLoadException("Cannot find meta data file '" + path + "' in the classpath");

        Properties metaProps = new Properties();
        try {
            metaProps.load(resource);
        } catch (IOException e) {
            try {
                resource.close();
            } catch (IOException ignored) {
            }
            throw new PluginLoadException("Error reading metadata properties file at '" + path + '"', e);
        }

        PluginMetaData pluginMetaData;
        try {
            pluginMetaData = new PluginMetaData(metaProps);
        } catch (IllegalArgumentException e) {
            throw new PluginLoadException("Error loading " + name + " meta data", e);
        }

        String implClassName = pluginMetaData.getImplementationClass();

        Class<?> implClass;
        try {
            implClass = Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            throw new PluginLoadException("'" + name + "'s implementation class '" + implClassName + "' is not on the classpath");
        }

        if (!MellowDPlugin.class.isAssignableFrom(implClass))
            throw new PluginLoadException("'" + name + "'s implementation class '" + implClassName + "' doesn't implement MellowDPlugin");

        MellowDPlugin plugin;
        try {
            plugin = (MellowDPlugin) implClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new PluginLoadException("Cannot instantiate '" + implClassName + "'", e);
        }

        plugin.onLoad();

        return plugin;
    }
}
