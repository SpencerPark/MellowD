package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.primitives.Dynamic;
import org.mellowd.midi.MIDIChannel;

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
        channel.setDynamic(dynamic);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
