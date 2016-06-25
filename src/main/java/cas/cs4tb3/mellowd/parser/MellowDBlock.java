package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.DynamicChange;
import cas.cs4tb3.mellowd.intermediate.GradualDynamicChange;
import cas.cs4tb3.mellowd.intermediate.Phrase;
import cas.cs4tb3.mellowd.intermediate.Playable;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.primitives.Beat;

import java.util.LinkedList;
import java.util.List;

public class MellowDBlock implements Playable {
    private final Memory localMemory;
    private final String name;
    private final boolean percussion;
    private List<Playable> elements;

    private Beat durationSinceGradualStart = null;
    private GradualDynamicChange gradualStart = null;

    public MellowDBlock(Memory globalMemory, String name, boolean percussion) {
        this.localMemory = new SymbolTable(globalMemory);
        this.name = name;
        this.percussion = percussion;
        this.elements = new LinkedList<>();
    }

    public Memory getLocalMemory() {
        return localMemory;
    }

    public String getName() {
        return name;
    }

    public boolean isPercussion() {
        return percussion;
    }

    public void add(DynamicChange dynamicChange) {
        if (gradualStart != null) {
            gradualStart.setEnd(dynamicChange.getDynamic());
            gradualStart.setChangeDuration(durationSinceGradualStart);
        } else {
            this.elements.add(dynamicChange);
        }
        durationSinceGradualStart = null;
        gradualStart = null;
    }

    public void add(GradualDynamicChange dynamicChange) {
        if (gradualStart != null) {
            gradualStart.setEnd(dynamicChange.getStart());
            gradualStart.setChangeDuration(durationSinceGradualStart);
        }
        durationSinceGradualStart = Beat.ZERO;
        gradualStart = dynamicChange;
    }

    public void add(Phrase phrase) {
        if (durationSinceGradualStart != null) {
            durationSinceGradualStart = new Beat(phrase.getDuration().getNumQuarters() + durationSinceGradualStart.getNumQuarters());
        }
        this.elements.add(phrase);
    }

    public void add(Playable playable) {
        if (playable instanceof Phrase)
            add(((Phrase) playable));
        else if (playable instanceof GradualDynamicChange)
            add(((GradualDynamicChange) playable));
        else if (playable instanceof DynamicChange)
            add(((DynamicChange) playable));
        else
            this.elements.add(playable);
    }

    @Override
    public void play(MIDIChannel channel) {
        this.elements.forEach(p -> p.play(channel));
        channel.finalizeEOT();
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        long duration = 0;
        for (Playable p : this.elements) {
            duration += p.calculateDuration(env);
        }
        return duration;
    }
}
