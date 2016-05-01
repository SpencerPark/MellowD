package cas.cs4tb3.mellowd.compiler;

import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;

public interface SequenceIODelegate {

    /**
     * Save the MIDI sequence to the given output file.
     * @param sequence the MIDI sequence to save
     * @param outFile the location to write the sequence to. This should not have an extension
     *                at the end as this save method will append the appropriate one based
     *                on the file type.
     * @throws IOException if an IO exception occurs during the saving process.
     */
    void save(Sequence sequence, File outFile) throws IOException;

    String getExtension();
}
