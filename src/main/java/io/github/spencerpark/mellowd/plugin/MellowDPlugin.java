package io.github.spencerpark.mellowd.plugin;

import io.github.spencerpark.mellowd.parser.MellowD;

public interface MellowDPlugin {

    public default void onLoad() {}
    public default void onUnload() {}

    public void apply(MellowD mellowD);
}
