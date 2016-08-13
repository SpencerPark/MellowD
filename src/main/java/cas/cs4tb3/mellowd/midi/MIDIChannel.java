package cas.cs4tb3.mellowd.midi;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.compiler.Compiler;
import cas.cs4tb3.mellowd.compiler.SequencePlayer;
import cas.cs4tb3.mellowd.intermediate.Phrase;
import cas.cs4tb3.mellowd.intermediate.Sound;
import cas.cs4tb3.mellowd.primitives.*;

import javax.sound.midi.*;
import java.util.*;

//A MIDIChannel wraps a javax.sound.midi.Track to provide virtual state information.
public class MIDIChannel {
    public enum NoteState {
        ON(true, false),
        OFF(false, false),
        ON_SLURRED(true, true),
        OFF_SLURRED(false, true);

        public final boolean isOn;
        public final boolean isSlurred;

        NoteState(boolean isOn, boolean isSlurred) {
            this.isOn = isOn;
            this.isSlurred = isSlurred;
        }

        public static NoteState getState(boolean isOn, boolean isSlurred) {
            if (isOn)
                return isSlurred ? NoteState.ON_SLURRED : NoteState.ON;
            else
                return isSlurred ? NoteState.OFF_SLURRED : NoteState.OFF;
        }
    }

    public static class ScheduledAction {
        private Runnable action;
        public final long stateTime;

        public ScheduledAction(Runnable action, long stateTime) {
            this.action = action;
            this.stateTime = stateTime;
        }

        public ScheduledAction andThen(Runnable run) {
            Runnable old = this.action;
            this.action = () -> {
                old.run();
                run.run();
            };
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduledAction that = (ScheduledAction) o;

            return stateTime == that.stateTime && action == that.action;
        }

        @Override
        public int hashCode() {
            int result = action.hashCode();
            result = 31 * result + (int) (stateTime ^ (stateTime >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "ScheduledAction{" +
                    "action=" + action +
                    ", stateTime=" + stateTime +
                    '}';
        }
    }

    public static final int DEFAULT_OFF_VELOCITY = 96;

    private final Track midiTrack;
    private final boolean isPercussion;
    private final int channelNum;
    private final TimingEnvironment timingEnvironment;
    private final SortedMap<Long, ScheduledAction> scheduledActions;
    private final PitchIndexedArray<MidiEvent> noteOffEvents;
    private final PitchIndexedArray<NoteState> noteStates;

    private int instrument = GeneralMidiInstrument.ACOUSTIC_GRAND_PIANO.midiNum();
    private int soundBank = GeneralMidiConstants.DEFAULT_SOUND_BANK;
    private Dynamic dynamic = Dynamic.mf; //The dynamic is mf by default
    private long stateTime = 0L;
    private int pitchBend = GeneralMidiConstants.NO_PITCH_BEND;
    private final Map<MIDIControl<?>, Object> controllers;
    private int octaveShift = 0;

    //This is a counter keeping track of the times that the slurred flag has been set
    //so that 2 calls to slurred require another 2 calls to un-slur.
    private boolean slurred = false;

    public MIDIChannel(Track midiTrack, boolean isPercussion, int channelNum, TimingEnvironment timingEnvironment) {
        this.midiTrack = midiTrack;
        this.isPercussion = isPercussion;
        this.channelNum = channelNum;
        this.timingEnvironment = timingEnvironment;
        this.scheduledActions = new TreeMap<>(Long::compare);
        this.controllers = new HashMap<>();

        this.noteStates = new PitchIndexedArray<>(NoteState.OFF);
        this.noteOffEvents = new PitchIndexedArray<>();
    }

    public boolean isPercussion() {
        return isPercussion;
    }

    protected int getChannelNum() {
        return channelNum;
    }

    protected Track getMidiTrack() {
        return midiTrack;
    }

    public long getStateTime() {
        return stateTime;
    }

    public TimingEnvironment getTimingEnvironment() {
        return timingEnvironment;
    }

    public long ticksInBeat(Beat beat) {
        return this.timingEnvironment.ticksInBeat(beat);
    }

    public Dynamic getDynamic() {
        return dynamic;
    }

    public Dynamic changeDynamic(int velocityMod) {
        return this.dynamic.louder(velocityMod);
    }

    public void setDynamic(Dynamic dynamic) {
        this.dynamic = dynamic;
    }

    public int getOctaveShift() {
        return this.octaveShift;
    }

    public void setOctaveShift(int octaveShift) {
        this.octaveShift = octaveShift;
    }

    public synchronized final long stepIntoFuture(long stateTimeMod) {
        long newTime = this.stateTime + stateTimeMod;

        Long earliest;
        while (!scheduledActions.isEmpty()
                && (earliest = scheduledActions.firstKey()) <= newTime) {
            this.stateTime = earliest;
            scheduledActions.remove(earliest).action.run();
        }

        return this.stateTime = newTime;
    }

    public synchronized final long stepIntoFuture(Beat beat) {
        return stepIntoFuture(this.timingEnvironment.ticksInBeat(beat));
    }

    public boolean isNoteOn(Pitch pitch) {
        return this.noteStates.get(pitch).isOn;
    }

    public boolean isPitchBent() {
        return this.pitchBend != GeneralMidiConstants.NO_PITCH_BEND;
    }

    public boolean isSlurred() {
        return this.slurred;
    }

    public NoteState getNoteState(Pitch pitch) {
        return this.noteStates.get(pitch);
    }

    @SuppressWarnings("unchecked")
    public <T> T getController(MIDIControl<T> controlType) {
        T controller = (T) this.controllers.get(controlType);

        if (controller == null) {
            controller = controlType.attachTo(this);
            this.controllers.put(controlType, controller);
        }

        return controller;
    }

    public void setSlurred(boolean slurred) {
        this.slurred = slurred;
    }

    public final void doLater(Beat timeOffset, Runnable action) {
        long stateTimeOffset = this.timingEnvironment.ticksInBeat(timeOffset);
        doLater(stateTimeOffset, action);
    }

    public final void doLater(long stateTimeOffset, Runnable action) {
        long time = this.stateTime + stateTimeOffset;
        this.scheduledActions.compute(time, (schTime, schAction) ->
                schAction == null ? new ScheduledAction(action, time) : schAction.andThen(action));
    }

    //This method should be called to put the EOT in the correct place. The EOT (end of track message)
    //marks the end of the music played on this track. The song stops playback when all of the tracks
    //have finished playing and therefor this message must be properly placed at the end with the invocation
    //of this method.
    public final void finalizeEOT() {
        midiTrack.add(new MidiEvent(Compiler.EOT_MESSAGE, this.stateTime + timingEnvironment.getPPQ()));
    }

    public final void setPitchBend(int bendAmt) {
        if (this.pitchBend == bendAmt) return;
        ShortMessage resetMessage;
        try {
            resetMessage = new ShortMessage(ShortMessage.PITCH_BEND, channelNum, 0x7F & bendAmt, 0x7F & (bendAmt >> 7));
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set pitch bend to " + bendAmt + ".", e);
        }
        this.midiTrack.add(new MidiEvent(resetMessage, this.stateTime));
        this.pitchBend = bendAmt;
    }

    public final void resetPitchBend() {
        setPitchBend(GeneralMidiConstants.NO_PITCH_BEND);
    }

    private void setSoundBank(int soundBank) {
        if (this.soundBank == soundBank) return;

        //Preform the sound bank change sequence described in the
        //[General MIDI Constants](../midi/GeneralMidiConstants.html).
        try {
            this.midiTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channelNum, GeneralMidiConstants.BANK_SELECT_CC_1, GeneralMidiConstants.BANK_SELECT_CC_1_VAL), stateTime));
            this.midiTrack.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channelNum, GeneralMidiConstants.BANK_SELECT_CC_2, soundBank), stateTime));
            this.soundBank = soundBank;
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set sound bank to " + soundBank + ".", e);
        }
    }

    private void setInstrument(int instrument) {
        if (this.instrument == instrument) return;

        //Add the program change message
        try {
            this.midiTrack.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channelNum, instrument, 0), stateTime));
            this.instrument = instrument;
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set the instrument to " + instrument + ".", e);
        }
    }

    public final void changeInstrument(GeneralMidiInstrument instrument) {
        setSoundBank(GeneralMidiConstants.DEFAULT_SOUND_BANK);
        setInstrument(instrument.midiNum());
    }

    public final void changeInstrument(int instrument, int soundBank) {
        setSoundBank(soundBank);
        setInstrument(instrument);
    }

    public void playNote(Pitch pitch, int velocityMod, long duration, int offVelocity) {
        if (pitch == Pitch.REST) return;
        Pitch toPlay = pitch.shiftOctave(this.getOctaveShift());

        if (isSlurred() && this.noteStates.get(toPlay).isSlurred) {
            MidiEvent offEvent = noteOffEvents.get(toPlay);
            if (offEvent != null) {
                //Skipping the last off and play the on softer
                this.midiTrack.remove(offEvent);
                noteOffEvents.set(toPlay, null);
            }

            this.noteOn(toPlay, (int) (-(dynamic.getVelocity() + velocityMod) / 3d));
        } else {
            //The note isn't slurred so it can be played normally
            this.noteOn(toPlay, velocityMod);
        }

        this.doLater(duration, () -> this.noteOff(toPlay, offVelocity));
    }

    public void playNote(Pitch pitch, int velocityMod, Beat duration, int offVelocity) {
        playNote(pitch, velocityMod, ticksInBeat(duration), offVelocity);
    }

    public void playNotes(Collection<Pitch> pitches, int velocityMod, Beat duration, int offVelocity) {
        long ticks = ticksInBeat(duration);
        pitches.forEach(p -> playNote(p, velocityMod, ticks, offVelocity));
    }

    public void playNotes(Collection<Pitch> pitches, int velocityMod, long duration, int offVelocity) {
        pitches.forEach(p -> playNote(p, velocityMod, duration, offVelocity));
    }

    protected final void noteOn(Pitch pitch, int velocityMod) {
        ShortMessage message;
        try {
            message = new ShortMessage(ShortMessage.NOTE_ON, channelNum, pitch.getMidiNum(), this.dynamic.louder(velocityMod).getVelocity());
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn note on (" + pitch.getMidiNum() + ") with dynamic of " + this.dynamic.louder(velocityMod).getVelocity() + ".", e);
        }
        this.midiTrack.add(new MidiEvent(message, this.stateTime));

        this.noteOffEvents.set(pitch, null);
        this.noteStates.set(pitch, NoteState.getState(true, isSlurred()));
    }

    protected final void noteOff(Pitch pitch, int offVelocity) {
        ShortMessage message;
        try {
            message = new ShortMessage(ShortMessage.NOTE_OFF, channelNum, pitch.getMidiNum(), offVelocity);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn note off (" + pitch.getMidiNum() + ") with dynamic of " + offVelocity + ".", e);
        }

        MidiEvent offEvent = new MidiEvent(message, this.stateTime);
        this.midiTrack.add(offEvent);
        this.noteOffEvents.set(pitch, offEvent);

        this.noteStates.set(pitch, NoteState.getState(false, isSlurred()));
    }

    public static void main(String[] args) throws InvalidMidiDataException, MidiUnavailableException {
        TimingEnvironment timingEnvironment = new TimingEnvironment(4, 4, 120);
        Sequence sequence = timingEnvironment.createSequence();
        Track track = sequence.createTrack();
        MIDIChannel channel = new MIDIChannel(track, false, 1, timingEnvironment);

        //channel.changeInstrument(GeneralMidiInstrument.BASSOON);
        channel.setDynamic(Dynamic.ffff);
        //channel.setSlurred(true);

        Pedal pedal = channel.getController(MIDIControl.SOSTENUTO);
        //portamento.twist(60);
        pedal.press();

        //channel.noteOn(Pitch.C);

        /*for (int i = 0; i < 5; i++) {
            if (i == 3) channel.setSlurred(false);
            Sound.newSound(Chord.major(Pitch.C), Beat.HALF, Articulation.NONE).play(channel);
        }*/
        Rhythm r = new Rhythm();
        Melody m = new Melody();
        m.add(new ArticulatedChord(Chord.major(Pitch.C)));

        System.out.println(r);
        System.out.println(m);
        new Phrase(m, r).play(channel);

        channel.finalizeEOT();

        SequencePlayer player = new SequencePlayer(MidiSystem.getSequencer(), sequence);
        player.playSync();
        player.close();
    }
}
