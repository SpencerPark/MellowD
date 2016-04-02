//Midi Note Message Source
//========================

package cas.cs4tb3.mellowd.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.Collection;

//This is a simple interface used to bind various pitch sources, mainly [Pitch](../Pitch.html) and
//[Chord](../primitives/Chord.html). As long as a note source is able to generate note on and off messages it
//can be used by the compiler to generate music.
public interface MidiNoteMessageSource {

    /**
     * Generate as many {@link ShortMessage#NOTE_ON note on} messages as required
     * to turn all notes in this source on.
     * @param channel the midi channel that the notes are to be played on
     * @param velocity the velocity that the notes should be played with
     * @return a Collection of the note on messages
     * @throws InvalidMidiDataException if the channel or velocity are not accepted by the
     * midi protocol.
     */
    Collection<ShortMessage> noteOn(int channel, int velocity) throws InvalidMidiDataException;
//
    /**
     * Generate as many {@link ShortMessage#NOTE_OFF note off} messages as required
     * to turn all notes in this source off.
     * @param channel the midi channel that the notes are already played on
     * @param velocity the velocity that the notes should be turn off with
     * @return a Collection of the note off messages
     * @throws InvalidMidiDataException if the channel or velocity are not accepted by the
     * midi protocol.
     */
    Collection<ShortMessage> noteOff(int channel, int velocity) throws InvalidMidiDataException;

    //Pitch evaluation needs to be delayed until an octave is know and the note source
    //must support shifting to the desired octave.
    MidiNoteMessageSource shiftOctave(int octave);
}
