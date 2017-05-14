package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.primitives.Dynamic;
import io.github.spencerpark.mellowd.midi.MIDIChannel;

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
