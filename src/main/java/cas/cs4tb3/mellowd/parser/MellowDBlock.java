package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.midi.TimingEnvironment;
import cas.cs4tb3.mellowd.intermediate.*;
import cas.cs4tb3.mellowd.intermediate.executable.CodeExecutor;
import cas.cs4tb3.mellowd.intermediate.executable.statements.Statement;
import cas.cs4tb3.mellowd.intermediate.variables.Memory;
import cas.cs4tb3.mellowd.intermediate.variables.SymbolTable;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.primitives.Beat;

import java.util.LinkedList;
import java.util.List;

public class MellowDBlock implements Output, ExecutionEnvironment {
    private final Memory globalMemory;
    private final Memory localMemory;
    private final String name;
    private final MIDIChannel channel;
    private final List<Statement> code;

    private Beat durationSinceGradualStart = null;
    private GradualDynamicChange gradualStart = null;

    public MellowDBlock(Memory globalMemory, String name, MIDIChannel channel) {
        this.globalMemory = globalMemory;
        this.localMemory = new SymbolTable(globalMemory);
        this.name = name;
        this.channel = channel;
        this.code = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public void addFragment(Statement block) {
        this.code.add(block);
    }

    public CodeExecutor createExecutor() {
        return new CodeExecutor(name, this, this, code);
    }

    @Override
    public boolean isPercussion() {
        return channel.isPercussion();
    }

    @Override
    public Memory getMemory(String... qualifier) {
        //TODO reference the memory of other source files
        if (qualifier.length == 1 && qualifier[0].equals("this")) {
            return globalMemory;
        }
        return localMemory;
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return channel.getTimingEnvironment();
    }

    public void add(DynamicChange dynamicChange) {
        if (gradualStart != null) {
            gradualStart.setEnd(dynamicChange.getDynamic());
            gradualStart.setChangeDuration(durationSinceGradualStart);
        } else {
            dynamicChange.play(channel);
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
        phrase.play(channel);
    }

    @Override
    public void put(Playable playable) {
        if (playable instanceof Phrase)
            add(((Phrase) playable));
        else if (playable instanceof GradualDynamicChange)
            add(((GradualDynamicChange) playable));
        else if (playable instanceof DynamicChange)
            add(((DynamicChange) playable));
        else if (playable != null)
            playable.play(channel);
        else
            throw new NullPointerException("Cannot put a null playable into the output");
    }

    @Override
    public long getStateTime() {
        return channel.getStateTime();
    }

    @Override
    public void close() {
        if (this.gradualStart != null) {
            throw new IllegalStateException("A "
                    + (this.gradualStart.isCrescendo() ? "crescendo" : "decrescendo")
                    + " was specified but a target was never given.");
        }

        channel.finalizeEOT();
    }
}
