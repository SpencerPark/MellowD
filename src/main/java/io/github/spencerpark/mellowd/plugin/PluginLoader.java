package io.github.spencerpark.mellowd.plugin;

public interface PluginLoader {

    public PluginMetaData getMetaData();

    public MellowDPlugin load();
}
