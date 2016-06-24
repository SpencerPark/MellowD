package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

/**
 * Created on 2016-06-21.
 */
public class InsrtumentChange implements Playable {
    private final int instrument;
    private final int soundbank;

    public InsrtumentChange(int instrument, int soundbank) {
        this.instrument = instrument;
        this.soundbank = soundbank;
    }

    public InsrtumentChange(int instrument) {
        this.instrument = instrument;
        this.soundbank = 0;
    }

    public InsrtumentChange(GeneralMidiInstrument instrument) {
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
