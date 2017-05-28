package org.mellowd.intermediate;

import java.io.Closeable;

public interface Output extends Closeable {

    void put(Playable playable);

    long getStateTime();

    @Override
    void close();
}
