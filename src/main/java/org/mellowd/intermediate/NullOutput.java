package org.mellowd.intermediate;

public class NullOutput implements Output {
    private static final NullOutput INSTANCE = new NullOutput();

    public static NullOutput getInstance() {
        return INSTANCE;
    }

    private NullOutput() { }

    @Override
    public void put(Playable playable) {

    }

    @Override
    public long getStateTime() {
        return 0;
    }

    @Override
    public void close() {

    }
}
