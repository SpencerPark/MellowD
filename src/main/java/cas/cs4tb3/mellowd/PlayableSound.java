//Playable Sound
//==============

package cas.cs4tb3.mellowd;

import cas.cs4tb3.mellowd.midi.MidiNoteMessageSource;

//A `PlayableSound` contains all of the information needed to preform a sound. It knows
//the pitches to play and how to play them from the [articulated sound](ArticulatedSound.html).
//It knows how long to play it from the [beat](Beat.html). It knows if it is slurred and
//it knows how loud to play it from the `velocity`. This class is an entity class that stores
//all of this information.
public class PlayableSound {
    private ArticulatedSound sound;
    private Beat duration;
    private boolean slurred;
    private int velocity = Dynamic.mf.getVelocity();

    public PlayableSound(ArticulatedSound sound, Beat duration, boolean slurred) {
        this.sound = sound;
        this.duration = duration;
        this.slurred = slurred;
    }

    private PlayableSound(ArticulatedSound sound, Beat duration, boolean slurred, int velocity) {
        this.sound = sound;
        this.duration = duration;
        this.slurred = slurred;
        this.velocity = velocity;
    }

    protected void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public Beat getDuration() {
        return duration;
    }

    public Articulation getArticulation() {
        return sound.getArticulation();
    }

    public boolean isSlurred() {
        return slurred;
    }

    public int getVelocity() {
        return velocity;
    }

    public MidiNoteMessageSource getSound() {
        return this.sound.getSound();
    }

    public PlayableSound inOctave(int octave) {
        return new PlayableSound(new ArticulatedSound(this.getSound().inOctave(octave), this.getArticulation()),
                this.duration,
                this.slurred,
                this.velocity);
    }
}
