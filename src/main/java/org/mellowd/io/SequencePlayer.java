//Sequence Player
//===============

package org.mellowd.io;

import javax.sound.midi.*;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

//This class is a simple playback manager that provides some concurrency features
//wrapped around the java midi sequencer. It provides `play()` and `stop()` methods
//for starting and stopping the sequence passed in at construction time.
public class SequencePlayer implements Closeable {
    //The `PLAYER_NUM` will be used to better track the various threads created for playback
    private static final AtomicInteger PLAYER_NUM = new AtomicInteger(0);
    //This is the midi message type for the end of track meta message
    private static final int END_OF_TRACK_MESSAGE = 0x2F;

    //Store the player and playback data
    private final Sequencer sequencer;
    private final Function<Synthesizer, Synthesizer> soundfontLoader;
    private final Sequence sequence;
    //Track the state of this player such that `true` &harr; music playing.
    private boolean isPlaying;
    private final ExecutorService player = Executors.newSingleThreadExecutor(r -> new Thread(r, "SequencePlayer-"+PLAYER_NUM.getAndIncrement()));

    //This constructor simply takes the desired sequence player and sequence to play
    public SequencePlayer(Sequencer sequencer, Function<Synthesizer, Synthesizer> soundfontLoader, Sequence sequence) {
        this.sequencer = sequencer;
        this.soundfontLoader = soundfontLoader;
        this.sequence = sequence;

        //To correctly update the `isPlaying` state variable we will register a listener
        //that sets the flag to false when it hears the sequencer end a track.
        sequencer.addMetaEventListener(meta -> isPlaying = (meta.getType() != END_OF_TRACK_MESSAGE));
    }

    //Here is the major part of this class. The `play()` method will play the sequence asynchronously
    //as the Sequencer already does but will also return a `Future` that will complete when the song
    //has finished playing or the playback has been aborted.
    public Future<SequencePlayer> play(long countIn) throws MidiUnavailableException, InvalidMidiDataException {
        //Firstly check if this player is already in action, we can only allow one playback at a time
        if (isPlaying) throw new IllegalStateException("Player is already playing.");

        //Now we can set up the sequencer by opening it up and putting in our midi sequence
        if (!sequencer.isOpen()) sequencer.open();
        sequencer.setSequence(sequence);

        Synthesizer synth = MidiSystem.getSynthesizer();
        if (!synth.isOpen()) synth.open();

        if (this.soundfontLoader != null)
            synth = this.soundfontLoader.apply(synth);

        //Connect the sequencer to the system synth so that any loaded soundfonts
        //get used
        List<Transmitter> transmitters = sequencer.getTransmitters();

        //If there are no transmitters attached to the sequencer then get a fresh
        //connection. Otherwise use the existing one to avoid a duplicate playback
        Transmitter transmitter;
        if (transmitters.isEmpty()) {
            transmitter = sequencer.getTransmitter();
        } else {
            transmitter = transmitters.get(0);
            for (int i = 1; i < transmitters.size(); i++)
                transmitters.get(i).close();
        }

        transmitter.setReceiver(synth.getReceiver());

        //Wait for the "countIn" time. The idea here is to possibly wait
        //for the jvm to catch up with itself so the playback is smooth at
        //the start.
        try {
            Thread.sleep(countIn);
        } catch (InterruptedException ignored) { }

        //Next we start playing the sequence and set the flag to reflect this.
        isPlaying = true;
        sequencer.start();

        //Here we are creating a virtual workload that is going to poll the `isPlaying` flag
        //every 50ms to check if the playback is complete. If it is the playback is finished and
        //the task completes. This allows for syncing with this task or polling its `isDone()`
        //method to integrate the playback into the application.
        return this.player.submit(() -> {
            while (isPlaying) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    break;
                }
            }
            SequencePlayer.this.stop();
        }, this);
    }

    //This convenience method just executes it's asynchronous counterpart and block until
    //it's completion.
    public void playSync(long countIn) throws InvalidMidiDataException, MidiUnavailableException {
        try {
            play(countIn).get();
        } catch (InterruptedException | ExecutionException ignore) { }
    }

    //Stop will halt the playback if a playback is occurring, otherwise it will do nothing.
    public void stop() {
        if (this.isPlaying) {
            this.isPlaying = false;
            this.sequencer.stop();
        }
    }

    @Override
    public void close() {
        stop();
        this.sequencer.close();
        this.player.shutdown();
    }
}
