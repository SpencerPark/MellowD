package org.mellowd.compiler;

import org.mellowd.intermediate.*;
import org.mellowd.intermediate.executable.CodeExecutor;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.SymbolTable;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.TimingEnvironment;
import org.mellowd.primitives.Beat;

import java.util.LinkedList;
import java.util.List;

public class MellowDBlock implements Output, ExecutionEnvironment {
    private final Memory blockLocals;
    private final String name;
    private final MIDIChannel channel;
    private final List<Statement> code;

    private Beat durationSinceGradualStart = null;
    private GradualDynamicChange gradualStart = null;

    public MellowDBlock(Memory globalMemory, String name, MIDIChannel channel) {
        this.blockLocals = new SymbolTable(globalMemory);
        // TODO support adding memory sections to others
        // this.blockLocals.set(QualifiedName.ofUnqualified("this"), globalMemory);
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

    protected MIDIChannel getMIDIChannel() {
        return this.channel;
    }

    public Memory getLocals() {
        return this.blockLocals;
    }

    @Override
    public boolean isPercussion() {
        return channel.isPercussion();
    }

    @Override
    public Memory getMemory() {
        return this.getLocals();
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

    public void clearCode() {
        this.code.clear();
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
