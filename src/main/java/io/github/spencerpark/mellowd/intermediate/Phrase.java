package io.github.spencerpark.mellowd.intermediate;

import io.github.spencerpark.mellowd.midi.TimingEnvironment;
import io.github.spencerpark.mellowd.midi.MIDIChannel;
import io.github.spencerpark.mellowd.midi.MIDIControl;
import io.github.spencerpark.mellowd.midi.Pedal;
import io.github.spencerpark.mellowd.primitives.Beat;
import io.github.spencerpark.mellowd.primitives.Melody;
import io.github.spencerpark.mellowd.primitives.Rhythm;

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
