package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.variables.AlreadyDefinedException;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;
import cas.cs4tb3.mellowd.intermediate.variables.UndefinedReferenceException;
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

    public void defineBlock(String name, boolean percussion) {
        MellowDBlock block = this.blocks.get(name);
        if (block == null) {
            block = new MellowDBlock(this.globalMemory, name, percussion);
            this.blocks.put(name, block);
        } else {
            throw new AlreadyDefinedException("A block with the name "+name+" is already defined.");
        }
    }

    public MellowDBlock getBlock(String name) {
        return this.blocks.get(name);
    }

    public Memory getGlobalMemory() {
        return globalMemory;
    }

    public TimingEnvironment getTimingEnvironment() {
        return timingEnvironment;
    }

    public Sequence record() {
        Queue<Integer> availableChannels = new LinkedList<>();
        availableChannels.addAll(GeneralMidiConstants.REGULAR_CHANNELS);
        int drumChannel = GeneralMidiConstants.DRUM_CHANNELS.iterator().next();

        Sequence sequence = this.timingEnvironment.createSequence();

        for (MellowDBlock block : this.blocks.values()) {
            Track track = sequence.createTrack();
            MIDIChannel channel = new MIDIChannel(track,
                    block.isPercussion(),
                    block.isPercussion() ? drumChannel : availableChannels.remove(),
                    this.timingEnvironment);
            block.play(channel);
        }

        return sequence;
    }
}
