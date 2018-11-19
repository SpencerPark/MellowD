package org.mellowd.compiler;

import org.mellowd.intermediate.executable.CodeExecutor;
import org.mellowd.intermediate.executable.statements.PercussionToggledEnvironment;
import org.mellowd.intermediate.functions.DefaultFunctions;
import org.mellowd.intermediate.functions.FunctionBank;
import org.mellowd.intermediate.variables.AlreadyDefinedException;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.SymbolTable;
import org.mellowd.io.CompositeSourceFinder;
import org.mellowd.io.SourceFinder;
import org.mellowd.midi.GeneralMidiConstants;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.TimingEnvironment;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MellowD implements ExecutionEnvironment {
    private SourceFinder srcFinder;

    private final PathMap<Memory> memory;
    private final Map<String, MellowDBlock> blocks;
    private final TimingEnvironment timingEnvironment;
    private final PathMap<FunctionBank> functions;

    private Sequence master;
    private final Queue<Integer> channelsAvailable;
    private final Queue<Integer> drumChannelsAvailable;

    public final ExecutionEnvironment PERCUSSION_TOGGLED_WRAPPER = new PercussionToggledEnvironment(this);

    public MellowD(SourceFinder finder, TimingEnvironment timingEnvironment) {
        this.srcFinder = finder;

        Memory globalMemory = new SymbolTable();
        this.memory = new PathMap<>(globalMemory);
        this.blocks = new HashMap<>();
        this.timingEnvironment = timingEnvironment;
        this.functions = new PathMap<>(new FunctionBank());
        addDefaultFunctionsToBank();

        this.master = timingEnvironment.createSequence();
        this.channelsAvailable = new LinkedList<>();
        this.channelsAvailable.addAll(GeneralMidiConstants.REGULAR_CHANNELS);
        this.drumChannelsAvailable = new LinkedList<>();
        this.drumChannelsAvailable.addAll(GeneralMidiConstants.DRUM_CHANNELS);
    }

    private void addDefaultFunctionsToBank() {
        FunctionBank bank = this.functions.get();
        DefaultFunctions.addAllToFunctionBank(bank);
    }

    public MellowDBlock defineBlock(String name, boolean percussion) {
        MellowDBlock block = this.blocks.get(name);
        if (block == null) {
            Integer channelNum;
            if (percussion) {
                channelNum = drumChannelsAvailable.poll();
                if (channelNum == null)
                    throw new Error("No drum channels available.");
                drumChannelsAvailable.offer(channelNum); //We want to cycle the drum channels
            } else {
                channelNum = channelsAvailable.poll();
                if (channelNum == null)
                    throw new IllegalStateException("Block definition "+name+" requires a channel but there are none left. Too many channels used.");
            }

            MIDIChannel channel = new MIDIChannel(this.master.createTrack(), percussion, channelNum, timingEnvironment);
            block = new MellowDBlock(this.memory.get(), name, channel);
            this.blocks.put(name, block);
        } else {
            throw new AlreadyDefinedException("A block with the name "+name+" is already defined.");
        }

        return block;
    }

    public MellowDBlock getBlock(String name) {
        return this.blocks.get(name);
    }

    public Memory getGlobalMemory() {
        return this.memory.get();
    }

    @Override
    public boolean isPercussion() {
        return false;
    }

    @Override
    public Memory getMemory(String... qualifier) {
        return this.memory.get(qualifier);
    }

    @Override
    public Memory createScope(String... qualifier) {
        return this.memory.putIfAbsent(SymbolTable::new, qualifier);
    }

    public SourceFinder getSrcFinder() {
        return this.srcFinder;
    }

    public void addSrcFinder(SourceFinder finder) {
        this.srcFinder = new CompositeSourceFinder(this.srcFinder, finder);
    }

    @Override
    public TimingEnvironment getTimingEnvironment() {
        return timingEnvironment;
    }

    public FunctionBank getFunctionBank(String... qualifier) {
        return this.functions.get(qualifier);
    }

    public FunctionBank getOrCreateFunctionBank(String... qualifier) {
        FunctionBank bank = this.functions.get(qualifier);

        if (bank == null) {
            bank = new FunctionBank();
            this.functions.put(bank, qualifier);
        }

        return bank;
    }

    public synchronized Sequence execute() throws Exception {
        this.blocks.values().forEach(block -> {
            Track oldTrack = block.getMIDIChannel().replaceTrack(this.master.createTrack());
            this.master.deleteTrack(oldTrack);
        });

        Queue<CodeExecutor> executors = new LinkedList<>();
        for (MellowDBlock block : this.blocks.values()) {
            CodeExecutor executor = block.createExecutor();
            executors.add(executor);
            executor.start();
        }

        try {
            while (!executors.isEmpty()) {
                CodeExecutor executor = executors.poll();
                executor.join(3000L); //Wait for the thread to finish
                if (executor.errorWhileExecuting()) {
                    throw executor.getExecutionError();
                }
            }
        } finally {
            this.blocks.values().forEach(MellowDBlock::clearCode);
        }

        return master;
    }
}
