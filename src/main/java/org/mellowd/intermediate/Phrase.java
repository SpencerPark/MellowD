package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.MIDIControl;
import org.mellowd.midi.Pedal;
import org.mellowd.primitives.Beat;
import org.mellowd.primitives.Melody;
import org.mellowd.primitives.Rhythm;

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
            boolean slurred = rhythm.isSlurred(i);

            if (slurred && !lastSlurred) {
                //This note is the first is a section of slurred components
                sustainPedal.press();
                channel.setSlurred(true);
            } else if (!slurred && lastSlurred) {
                //This note is the first of a section of non-slurred components
                channel.setSlurred(false);
                sustainPedal.release();
            }

            Sound sound = melody.getElementAtIndex(i).createSound(rhythm.getAtIndex(i));
            sound.play(channel);

            //Update the tracker
            lastSlurred = slurred;
        }

        // Always clear slur at the end because the slur cannot cross a phrase boundary.
        channel.setSlurred(false);
        sustainPedal.release();
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        int numBeats = rhythm.size();
        int numNotes = melody.size();
        int numElements = Math.max(numBeats, numNotes);

        long ticks = 0;
        for (int i = 0; i < numElements; i++) {
            ticks += env.ticksInBeat(this.rhythm.getAtIndex(i));
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
