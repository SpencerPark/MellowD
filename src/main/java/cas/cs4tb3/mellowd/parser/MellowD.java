package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;
import cas.cs4tb3.mellowd.midi.GeneralMidiConstants;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MellowD {
    private final Memory globalMemory;
    private final Map<String, MellowDBlock> blocks;
    private final TimingEnvironment timingEnvironment;

    public MellowD(TimingEnvironment timingEnvironment) {
        this.globalMemory = new SymbolTable();
        this.blocks = new HashMap<>();
        this.timingEnvironment = timingEnvironment;
    }

    public MellowDBlock getBlock(String name) {
        MellowDBlock block = this.blocks.get(name);
        if (block == null) {
            block = new MellowDBlock(this.globalMemory, name);
            this.blocks.put(name, block);
        }
        return block;
    }

    public Memory getGlobalMemory() {
        return globalMemory;
    }

    public TimingEnvironment getTimingEnvironment() {
        return timingEnvironment;
    }

    public Sequence record() {
        //TODO better channel selection and creation
        Queue<Integer> availableChannels = new LinkedList<>();
        availableChannels.addAll(GeneralMidiConstants.REGULAR_CHANNELS);
        int drumChannel = GeneralMidiConstants.DRUM_CHANNELS.iterator().next();

        Sequence sequence = this.timingEnvironment.createSequence();

        for (MellowDBlock block : this.blocks.values()) {
            Track track = sequence.createTrack();
            int channelNum = availableChannels.remove();
            Boolean percussion = block.getLocalMemory().get("percussion", Boolean.class);
            if (percussion == null) percussion = false;
            MIDIChannel channel = new MIDIChannel(track,
                    percussion,
                    percussion ? drumChannel : channelNum,
                    this.timingEnvironment);
            block.play(channel);
        }

        return sequence;
    }
}
