package org.mellowd.intermediate;

import org.mellowd.primitives.Articulation;
import org.mellowd.primitives.Beat;
import org.mellowd.primitives.Pitch;
import org.mellowd.primitives.Chord;
import org.mellowd.midi.GeneralMidiConstants;
import org.mellowd.midi.Knob;
import org.mellowd.midi.MIDIChannel;
import org.mellowd.midi.MIDIControl;

public abstract class ArticulatedSound extends Sound {
    protected final Articulation articulation;

    private ArticulatedSound(Chord chord, Beat duration, Articulation articulation) {
        super(chord, duration);
        this.articulation = articulation;
    }

    private ArticulatedSound(Pitch pitch, Beat duration, Articulation articulation) {
        super(pitch, duration);
        this.articulation = articulation;
    }

    public Articulation getArticulation() {
        return articulation;
    }

    public static ArticulatedSound newSound(Chord chord, Beat beat, Articulation articulation) {
        switch (articulation) {
            default:
                throw new IllegalArgumentException("Cannot create an ArticulatedSound without an articulation.");
            case STACCATO:
                return new Staccato(chord, beat);
            case STACCATISSIMO:
                return new Staccatissimo(chord, beat);
            case MARCATO:
                return new Marcato(chord, beat);
            case ACCENT:
                return new Accent(chord, beat);
            case TENUTO:
                return new Tenuto(chord, beat);
            case GLISCANDO:
                return new Gliscando(chord, beat);
        }
    }

    public static ArticulatedSound newSound(Pitch pitch, Beat beat, Articulation articulation) {
        switch (articulation) {
            default:
                throw new IllegalArgumentException("Cannot create an ArticulatedSound without an articulation.");
            case STACCATO:
                return new Staccato(pitch, beat);
            case STACCATISSIMO:
                return new Staccatissimo(pitch, beat);
            case MARCATO:
                return new Marcato(pitch, beat);
            case ACCENT:
                return new Accent(pitch, beat);
            case TENUTO:
                return new Tenuto(pitch, beat);
            case GLISCANDO:
                return new Gliscando(pitch, beat);
        }
    }

    public static class Staccato extends ArticulatedSound {

        public Staccato(Chord chord, Beat duration) {
            super(chord, duration, Articulation.STACCATO);
        }

        public Staccato(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.STACCATO);
        }

        @Override
        public void play(MIDIChannel channel) {
            Knob releaseTime = channel.getController(MIDIControl.RELEASE_TIME);
            releaseTime.twist(128 / 4);

            //Staccato makes the performance short and choppy. Described in jazz
            //as `dit`. To achieve this effect the duration will be chopped to a
            //forth of its value and the note will be ended very quickly.
            long tickDuration = super.getDuration(channel);
            tickDuration /= 4;

            channel.playNotes(pitches, 0, tickDuration, GeneralMidiConstants.MAX_VELOCITY);
            channel.stepIntoFuture(duration);

            releaseTime.twist(128 / 2);
        }
    }

    public static class Staccatissimo extends ArticulatedSound {
        private static final int VOLUME_INCREASE = 8;

        public Staccatissimo(Chord chord, Beat duration) {
            super(chord, duration, Articulation.STACCATISSIMO);
        }

        public Staccatissimo(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.STACCATISSIMO);
        }

        @Override
        public void play(MIDIChannel channel) {
            Knob releaseTime = channel.getController(MIDIControl.RELEASE_TIME);
            releaseTime.twist(0);

            //Staccatissimo makes the performance short but more powerful. It is
            //given some more emphasis. It is similar to staccato but the duration
            //is going to be chopped to a third (rather than a forth) and it will be
            //played with a bit more velocity.
            long tickDuration = super.getDuration(channel);
            tickDuration /= 3;

            channel.playNotes(pitches, VOLUME_INCREASE, tickDuration, GeneralMidiConstants.MAX_VELOCITY);
            channel.stepIntoFuture(duration);

            releaseTime.twist(128 / 2);
        }
    }

    public static class Marcato extends ArticulatedSound {
        private static final int VOLUME_INCREASE = 12;

        public Marcato(Chord chord, Beat duration) {
            super(chord, duration, Articulation.MARCATO);
        }

        public Marcato(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.MARCATO);
        }

        @Override
        public void play(MIDIChannel channel) {
            Knob releaseTime = channel.getController(MIDIControl.RELEASE_TIME);
            Knob attackTime = channel.getController(MIDIControl.ATTACK_TIME);
            attackTime.twist(10);
            releaseTime.twist(60);

            //Marcato is the same a staccato but with more power. It is referred to
            //as `dhat` by jazz musicians and to preform a note with articulated with marcato
            //the note's duration will be chopped to a third, the velocity will be increased
            //and the note will be release very quickly.
            long tickDuration = super.getDuration(channel);
            tickDuration /= 3;

            channel.playNotes(pitches, VOLUME_INCREASE, tickDuration, GeneralMidiConstants.MAX_VELOCITY);
            channel.stepIntoFuture(duration);

            attackTime.twist(0);
            releaseTime.twist(128 / 2);
        }
    }

    public static class Accent extends ArticulatedSound {
        private static final int VOLUME_INCREASE = 16;
        private static final int OFF_VELOCITY = 113;

        public Accent(Chord chord, Beat duration) {
            super(chord, duration, Articulation.ACCENT);
        }

        public Accent(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.ACCENT);
        }

        @Override
        public void play(MIDIChannel channel) {
            //An accent is played by attacking the note. This gives it a much faster velocity and
            //will also drop off a bit quicker than the average note. This is sometimes referred to
            //as `dah` by jazz musicians.

            channel.playNotes(pitches, VOLUME_INCREASE, duration, OFF_VELOCITY);
            channel.stepIntoFuture(duration);
        }
    }

    public static class Tenuto extends ArticulatedSound {
        private static final int OFF_VELOCITY = GeneralMidiConstants.MIN_VELOCITY + 1;

        public Tenuto(Chord chord, Beat duration) {
            super(chord, duration, Articulation.TENUTO);
        }

        public Tenuto(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.TENUTO);
        }

        @Override
        public void play(MIDIChannel channel) {
            //Tenuto is the equivalent of a single note slur. It is also called `doo` by jazz musicians
            //and so in order to preform a tenuto note the note will be let off as slow as possible with
            //a slightly longer duration.
            long tickDuration = super.getDuration(channel);
            tickDuration += tickDuration / 8;

            channel.playNotes(pitches, 0, tickDuration, OFF_VELOCITY);
            channel.stepIntoFuture(duration);
        }
    }

    public static class Gliscando extends ArticulatedSound {
        //This declaration specifies the amount to bend the pitch each glissando step
        private static final int BEND_AMT = 128;
        private static final int BEND_STEPS = 16;

        //Keep a reference this sound's pitch so that we can determine if we
        //should bend up or down depending on the next.
        private boolean bendUp = true;

        public Gliscando(Chord chord, Beat duration) {
            super(chord, duration, Articulation.GLISCANDO);
        }

        public Gliscando(Pitch pitch, Beat duration) {
            super(pitch, duration, Articulation.GLISCANDO);
        }

        public void setBendUp(boolean bendUp) {
            this.bendUp = bendUp;
        }

        @Override
        public void play(MIDIChannel channel) {
            //Gliscando is a glide. It can be preformed as a pitch bend. To preform a gliscando a total of
            //16 pitch bend changes will give the effect that the note is falling or climbing (depending
            //on the direction of bend). These changes will be equally spaced over the duration of the note
            //as to not interfere with the next note. Additionally a reset message will be queued for the
            //next note to take.

            long tickDuration = super.getDuration(channel);
            //If the sound is a chord a gliscando should be preformed as a roll.
            if (super.isChord()) {
                //The roll will play the first note in the chord right on the down beat. Each
                //consecutive note in the chord will be delayed by the `offsetStep`.
                long offsetStep = tickDuration / (super.getNumNotes() * 4);
                long offset = 0;
                for (Pitch p : super.pitches) {
                    long duration = tickDuration - offset;
                    channel.doLater(offset, () ->
                            channel.playNote(p, 0, duration, MIDIChannel.DEFAULT_OFF_VELOCITY));
                    offset += offsetStep;
                }
                //otherwise preform the gliscando as a pitch bend.
            } else {
                channel.playNotes(pitches, 0, duration, MIDIChannel.DEFAULT_OFF_VELOCITY);
                for (int offset = 0; offset < BEND_STEPS; offset++) {
                    int bendAmount = GeneralMidiConstants.NO_PITCH_BEND + ( offset * ( bendUp ? BEND_AMT : -BEND_AMT ) );
                    channel.doLater(tickDuration * offset / BEND_STEPS, () -> channel.setPitchBend(bendAmount));
                }
                channel.doLater(super.getDuration(channel), channel::resetPitchBend);
            }

            channel.stepIntoFuture(duration);
        }
    }
}
