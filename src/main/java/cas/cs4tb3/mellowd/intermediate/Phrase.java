package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.midi.MIDIControl;
import cas.cs4tb3.mellowd.midi.Pedal;
import cas.cs4tb3.mellowd.primitives.Beat;
import cas.cs4tb3.mellowd.primitives.Melody;
import cas.cs4tb3.mellowd.primitives.Rhythm;

public class Phrase implements Playable {
    protected final Melody melody;
    protected final Rhythm rhythm;

    public Phrase(Melody melody, Rhythm rhythm) {
        this.melody = melody;
        this.rhythm = rhythm;
    }

    public Melody getMelody() {
        return melody;
    }

    public Rhythm getRhythm() {
        return rhythm;
    }

    public Beat getDuration() {
        return rhythm.getDuration();
    }

    @Override
    public void play(MIDIChannel channel) {
        int numBeats = rhythm.size();
        int numNotes = melody.size();
        int numElements = Math.max(numBeats, numNotes);

        if (numBeats == 0 || numNotes == 0) return;

        boolean lastSlurred = false;

        Pedal sustainPedal = channel.getController(MIDIControl.SUSTAIN);
        for (int i = 0; i < numElements; i++) {
            boolean slurred = rhythm.isSlurred(i % numBeats);

            if (slurred && !lastSlurred) {
                //This note is the first is a section of slurred components
                sustainPedal.press();
                channel.setSlurred(true);
            } else if (!slurred && lastSlurred) {
                //This note is the first of a section of non-slurred components
                channel.setSlurred(false);
                sustainPedal.release();
            }

            Sound sound = melody.getAt(i % numNotes).createSound(rhythm.getBeat(i % numBeats));
            sound.play(channel);

            //Update the tracker
            lastSlurred = slurred;
        }
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        int numBeats = rhythm.size();
        int numNotes = melody.size();
        int numElements = Math.max(numBeats, numNotes);

        long ticks = 0;
        for (int i = 0; i < numElements; i++) {
            ticks += env.ticksInBeat(this.rhythm.getBeat(i % numBeats));
        }

        return ticks;
    }

    @Override
    public String toString() {
        return "Phrase{" +
                "melody=" + melody +
                ", rhythm=" + rhythm +
                '}';
    }
}
