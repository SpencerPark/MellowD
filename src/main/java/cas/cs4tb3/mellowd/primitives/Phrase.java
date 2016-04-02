//Phrase
//======

package cas.cs4tb3.mellowd.primitives;

import cas.cs4tb3.mellowd.PlayableSound;

//A phrase is a collection of sounds with rhythm. Each sound has a duration.
//See [PlayableSound](../PlayableSound.html) for more details.
//In a Mellow D source file a phrase is a `melody` `*` `rhythm`. The [Rhythm](Rhythm.html)
//has a method for creating a phrase from a [Melody](Melody.html) that the compiler
//makes use of.
public class Phrase {
    //`sounds` is the data for this class. It is simply an array of sounds.
    private PlayableSound[] sounds;

    public Phrase(PlayableSound... sounds) {
        this.sounds = sounds;
    }

    public PlayableSound[] getSounds() {
        return sounds;
    }

    public Phrase shiftOctave(int octaveShift) {
        PlayableSound[] sounds = new PlayableSound[this.sounds.length];
        for (int i = 0; i < this.sounds.length; i++) {
            sounds[i] = this.sounds[i].shiftOctave(octaveShift);
        }
        return new Phrase(sounds);
    }
}
