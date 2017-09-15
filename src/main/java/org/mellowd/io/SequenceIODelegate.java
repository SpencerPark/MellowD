package org.mellowd.io;

import javax.sound.midi.Sequence;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface SequenceIODelegate {

    /**
     * Save the MIDI sequence to the given output file.
     * @param sequence the MIDI sequence to save
     * @param outFile the location to write the sequence to. This should not have an extension
     *                at the end as this save method will append the appropriate one based
     *                on the file type.
     * @throws IOException if an IO exception occurs during the saving process.
     */
    default void save(Sequence sequence, File outFile) throws IOException {
        save(sequence, new FileOutputStream(outFile));
    }

    /**
     * Save the MIDI sequence to the given output file.
     * @param sequence the MIDI sequence to save
     * @param out the stream to save the sequence to
     * @throws IOException if an IO exception occurs during the saving process.
     */
    void save(Sequence sequence, OutputStream out) throws IOException;

    String getExtension();
}
