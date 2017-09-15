package org.mellowd.io;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 2016-04-20.
 */
public class MIDIIODelegate implements SequenceIODelegate {
    private static final MIDIIODelegate instance = new MIDIIODelegate();

    public static MIDIIODelegate getInstance() {
        return instance;
    }

    private MIDIIODelegate() { }

    @Override
    public void save(Sequence sequence, OutputStream out) throws IOException {
        //Write the result to the out file. The type 1 midi format is the standard
        //type for multi track sequences which we have in our case.
        MidiSystem.write(sequence, 1, out);
    }

    @Override
    public String getExtension() {
        return ".mid";
    }
}
