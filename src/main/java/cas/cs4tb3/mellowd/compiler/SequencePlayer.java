//Sequence Player
//===============

package cas.cs4tb3.mellowd.compiler;

import javax.sound.midi.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//This class is a simple playback manager that provides some concurrency features
//wrapped around the java midi sequencer. It provides `play()` and `stop()` methods
//for starting and stopping the sequence passed in at construction time.
public class SequencePlayer {
    //The `PLAYER_NUM` will be used to better track the various threads created for playback
    private static final AtomicInteger PLAYER_NUM = new AtomicInteger(0);
    //This is the midi message type for the end of track meta message
    private static final int END_OF_TRACK_MESSAGE = 0x2F;

    //Store the player and playback data
    private final Sequencer sequencer;
    private final Sequence sequence;
    //Track the state of this player such that `true` &harr; music playing.
    private boolean isPlaying;

    //This constructor simply takes the desired sequence player and sequence to play
    public SequencePlayer(Sequencer sequencer, Sequence sequence) {
        this.sequencer = sequencer;
        this.sequence = sequence;

        //To correctly update the `isPlaying` state variable we will register a listener
        //that sets the flag to false when it hears the sequencer end a track.
        sequencer.addMetaEventListener(new MetaEventListener() {
            @Override
            public void meta(MetaMessage meta) {
                if (meta.getType() == END_OF_TRACK_MESSAGE) {
                    isPlaying = false;
                }
            }
        });
    }

    //Here is the major part of this class. The `play()` method will play the sequence asynchronously
    //as the Sequencer already does but will also return a `Future` that will complete when the song
    //has finished playing or the playback has been aborted.
    public Future<SequencePlayer> play() throws MidiUnavailableException, InvalidMidiDataException {
        //Firstly check if this player is already in action, we can only allow one playback at a time
        if (isPlaying) throw new IllegalStateException("Player is already playing.");

        //Now we can set up the sequencer by opening it up and putting in our midi sequence
        if (!sequencer.isOpen()) sequencer.open();
        sequencer.setSequence(sequence);

        //Next we start playing the sequence and set the flag to reflect this.
        isPlaying = true;
        sequencer.start();

        //Here we are creating a virtual workload that is going to poll the `isPlaying` flag
        //every 50ms to check if the playback is complete. If it is the playback is finished and
        //the task completes. This allows for syncing with this task or polling its `isDone()`
        //method to integrate the playback into the application.
        final FutureTask<SequencePlayer> player = new FutureTask<SequencePlayer>(new Runnable() {
            @Override
            public void run() {
                while (isPlaying) {
                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, this) {
            @Override
            protected void done() {
                stop();
            }
        };

        //Lastly we can startup a new thread and run the worker on it.
        new Thread("SequencePlayer-"+PLAYER_NUM.getAndIncrement()) {
            @Override
            public void run() {
                player.run();
            }
        }.start();
        return player;
    }

    //This convenience method just executes it's asynchronous counterpart and block until
    //it's completion.
    public void playSync() throws InvalidMidiDataException, MidiUnavailableException {
        try {
            play().get();
        } catch (InterruptedException | ExecutionException ignore) { }
    }

    //Stop will halt the playback if a playback is occurring, otherwise it will do nothing.
    public void stop() {
        if (this.isPlaying) {
            this.isPlaying = false;
            this.sequencer.stop();
        }
    }
}
