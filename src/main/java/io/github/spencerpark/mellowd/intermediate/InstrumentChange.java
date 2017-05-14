package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.GeneralMidiInstrument;
import io.github.spencerpark.mellowd.midi.MIDIChannel;

public class InstrumentChange implements Playable {
    private final int instrument;
    private final int soundbank;

    public InstrumentChange(int instrument, int soundbank) {
        this.instrument = instrument;
        this.soundbank = soundbank;
    }

    public InstrumentChange(int instrument) {
        this.instrument = instrument;
        this.soundbank = 0;
    }

    public InstrumentChange(GeneralMidiInstrument instrument) {
        this.instrument = instrument.midiNum();
        this.soundbank = 0;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.changeInstrument(this.instrument, this.soundbank);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
