//Time Signature
//==============

package cas.cs4tb3.mellowd.midi;

import cas.cs4tb3.mellowd.primitives.Beat;

import javax.sound.midi.*;

//The `TimingEnvironment` class handles timing information for a sequence. It allows for
//conversion from a classical timing definition of a beat to a MIDI tick count. It is
//also responsible for modifying the time signature and tempo of the sequence with the
//creation of the appropriate MIDI meta messages. See
public class TimingEnvironment {
    //The MIDI meta message identifier for a time signature message.
    private static final byte TIME_SIGNATURE_MIDI_SUBTYPE = 0x58;

    //The MIDI meta message identifier for a tempo message.
    private static final byte TEMPO_MIDI_SUBTYPE = 0x51;

    //The number of microseconds in each minute (60,000,000). This constant aids
    //in calculating the microseconds per beat for a BPM conversion.
    private static final int MICROSECONDS_PER_MINUTE = 60000000;

    //This constant is the number of ticks that need to pass on the MIDI clock for the
    //metronome to click. This clock is independent from the sequencer clock. Redefining
    //the PPQN should not affect this clock. Therefore we will leave it at the standard 24 ticks
    //per click.
    private static final byte TICKS_PER_METER_CLICK = 24;

    //This constant appears in the time signature midi message. <sup>1</sup>&frasl;<sub>4</sub>
    //consists of 8 <sup>1</sup>&frasl;<sub>32</sub>. There should be no reason to change this.
    private static final byte THIRTY_SECOND_NOTES_PER_QUARTER = 8;

    //The `PPQN` is the Pulses Per Quarter Note. It is also referred to as simply `PPQ`.
    //It will be 960 by default for all sequences but reserve the right to change it
    //if we need a higher resolution. This is the number of clock ticks that pass over
    //the duration of a single quarter note.
    private static final int DEFAULT_PPQN = 960;

    //Store all of the timing information. The time signature is
    //<sup>timeSigNum</sup>&frasl;<sub>timeSigDen</sub>. Our pluses per quarter note
    //and our tempo.
    private final byte timeSigNum;
    private final byte timeSigDen;
    private final int ppqn;
    private final int bpm;

    //The numerator and denominator for the time signature gives the synthesiser some hints
    //on what are the down and up beats. The bpm is the tempo.
    public TimingEnvironment(int numerator, int denominator, int bpm) {
        this.timeSigNum = (byte) numerator;
        this.timeSigDen = (byte) denominator;
        this.ppqn = DEFAULT_PPQN;
        this.bpm = bpm;
    }

    public Sequence createSequence() {
        try {
            Sequence s = new Sequence(Sequence.PPQ, ppqn);
            Track timeDataTrack = s.createTrack();
            timeDataTrack.add(new MidiEvent(this.timeSignatureMessage(), 0));
            timeDataTrack.add(new MidiEvent(this.tempoMessage(), 0));
            return s;
        } catch (InvalidMidiDataException e) {
            throw new RuntimeException(e);
        }
    }

    //Get the number of ticks per quarter note. This is the resolution of the timing.
    public int getPPQ() {
        return ppqn;
    }

    //Get the tempo in beats per minute.
    public int getTempo() {
        return this.bpm;
    }

    //The `PPQN` was chosen as to support triplets on an integer number of
    //ticks but the resolution of other ratios will vary and some precision
    //may be lost when cast to a long.
    public long ticksInBeat(Beat beat) {
        return (long) (ppqn * beat.getNumQuarters());
    }

    //Create a MIDI message that can be sent to set the time signature of
    //the sequence to this time signature.
    public MetaMessage timeSignatureMessage() {
        MetaMessage timeSigMessage = null;
        try {
            timeSigMessage = new MetaMessage(TIME_SIGNATURE_MIDI_SUBTYPE, new byte[] {
                    timeSigNum,
                    (byte) (Math.log(timeSigDen) / Math.log(2)),
                    TICKS_PER_METER_CLICK,
                    THIRTY_SECOND_NOTES_PER_QUARTER
            }, 4);
        } catch (InvalidMidiDataException ignored) {
            /* Will never happen as the message type is a defined constant */
        }
        return timeSigMessage;
    }

    //Create a MIDI message that can be sent to set the tempo of
    //the sequence to this tempo.
    public MetaMessage tempoMessage() {
        int microSecPerBeat = MICROSECONDS_PER_MINUTE / bpm;
        MetaMessage tempoMessage = null;
        try {
            tempoMessage = new MetaMessage(TEMPO_MIDI_SUBTYPE, new byte[] {
                    (byte) ((microSecPerBeat >>> 16) & 0xFF),
                    (byte) ((microSecPerBeat >>> 8 ) & 0xFF),
                    (byte) ((microSecPerBeat       ) & 0xFF)
            }, 3);
        } catch (InvalidMidiDataException ignored) {
            /* Will never happen as the message type is a defined constant */
        }
        return tempoMessage;
    }
}
