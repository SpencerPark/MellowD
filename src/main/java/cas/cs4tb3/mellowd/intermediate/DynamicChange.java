package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.Dynamic;

/**
 * Created on 2016-06-15.
 */
public class DynamicChange implements Playable {
    private final Dynamic dynamic;

    public DynamicChange(Dynamic dynamic) {
        this.dynamic = dynamic;
    }

    public Dynamic getDynamic() {
        return dynamic;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.setVelocity(dynamic);
    }
}
