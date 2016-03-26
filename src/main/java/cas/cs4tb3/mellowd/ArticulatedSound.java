//Articulated Sound
//=================

package cas.cs4tb3.mellowd;

import cas.cs4tb3.mellowd.midi.MidiNoteMessageSource;

//An articulated sound is any [note source](../midi/MidiNoteMessageSource.html) tweaked with
//an [articulation](Articulation.html).
public class ArticulatedSound {
    private MidiNoteMessageSource sound;
    private Articulation articulation;

    //A simple constructor directly setting the field values.
    public ArticulatedSound(MidiNoteMessageSource sound, Articulation articulation) {
        this.sound = sound;
        this.articulation = articulation;
    }

    //A constructor setting the sound field value with no articulation.
    public ArticulatedSound(MidiNoteMessageSource sound) {
        this.sound = sound;
        this.articulation = Articulation.NONE;
    }

    //The rest of this class is getter and setters for the fields.
    public MidiNoteMessageSource getSound() {
        return sound;
    }

    public Articulation getArticulation() {
        return articulation;
    }

    public void setArticulation(Articulation articulation) {
        this.articulation = articulation;
    }
}
