package cas.cs4tb3.mellowd.compiler;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 * Created on 2016-04-30.
 */
public class TimeStampedMIDIMessage {
    private final long timestamp;
    private final MidiMessage message;

    public TimeStampedMIDIMessage(long timestamp, MidiMessage message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public MidiMessage getMessage() {
        return message;
    }

    public void feedTo(Receiver midiReceiver) {
        midiReceiver.send(message, timestamp);
    }
}
