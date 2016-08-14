package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.primitives.Dynamic;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

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
