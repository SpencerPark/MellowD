package org.mellowd.plugin;

import org.mellowd.parser.MellowD;

@FunctionalInterface
public interface MellowDPlugin {

    public default void onLoad() {}
    public default void onUnload() {}

    public void apply(MellowD mellowD);
}
