package org.mellowd.midi;

import org.mellowd.io.Compiler;
import org.mellowd.primitives.Beat;
import org.mellowd.primitives.Dynamic;
import org.mellowd.primitives.Pitch;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
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

    private volatile MIDITrack midiTrack;
    private final boolean percussion;
    private final int channelNum;
    private final TimingEnvironment timingEnvironment;
    private SortedMap<Long, ScheduledAction> scheduledActions;
    private final PitchIndexedArray<MidiEvent> noteOffEvents;
    private final PitchIndexedArray<NoteState> noteStates;

    private int instrument = GeneralMidiInstrument.ACOUSTIC_GRAND_PIANO.midiNum();
    private int soundBank = GeneralMidiConstants.DEFAULT_SOUND_BANK;
    private Dynamic dynamic = Dynamic.mf; //The dynamic is mf by default
    private long stateTime = 0L;
    private int pitchBend = GeneralMidiConstants.NO_PITCH_BEND;
    private final Map<MIDIControl<? extends MIDIController>, MIDIController> controllers;
    private int octaveShift = 0;
    private int transposeShift = 0;
    private boolean muted = false;

    private boolean slurred = false;

    public MIDIChannel(MIDITrack midiTrack, boolean percussion, int channelNum, TimingEnvironment timingEnvironment) {
        this.midiTrack = midiTrack;
        this.percussion = percussion;
        this.channelNum = channelNum;
        this.timingEnvironment = timingEnvironment;
        this.scheduledActions = new TreeMap<>(Long::compare);
        this.controllers = new HashMap<>();

        this.noteStates = new PitchIndexedArray<>(NoteState.OFF);
        this.noteOffEvents = new PitchIndexedArray<>();
    }

    public MIDITrack replaceTrack(MIDITrack newTrack) {
        // TODO simply replay the latest of all configuration messages?
        MIDITrack oldTrack = this.midiTrack;
        long timeDiff = this.stateTime;

        this.stateTime = 0L;
        this.midiTrack = newTrack;

        // Reschedule all of the actions to be relative to the new reset time
        SortedMap<Long, ScheduledAction> rolledBackActions = new TreeMap<>(Long::compare);
        this.scheduledActions.values().forEach(action -> {
            long rolledBackTime = action.stateTime - timeDiff;
            ScheduledAction rolledBackAction = new ScheduledAction(action.action, rolledBackTime);
            rolledBackActions.put(rolledBackTime, rolledBackAction);
        });
        this.scheduledActions = rolledBackActions;

        // Clear the note off events because they don't exist on the new track
        this.noteOffEvents.setAll(null);

        // Turn on all notes that should be on at the average velocity
        // TODO this velocity should not be arbitrary. Consider even excluding this and leaving the notes off
        this.noteStates.forEach((pitch, state) -> {
            if (state.isOn) {
                this.noteOn(pitch, Dynamic.mf.getVelocity());
            }
        });

        // If not a percussion block make sure to reapply the last instrument change
        if (!this.percussion) {
            // Forcefully set the instrument. Set to -1 to disable the optimization that skips
            // setting the instrument when it is the same as the channel's state
            int soundBank = this.soundBank;
            this.soundBank = -1;
            int instrument = this.instrument;
            this.instrument = -1;
            this.setSoundBank(soundBank);
            this.setInstrument(instrument);
        }

        if (this.pitchBend != GeneralMidiConstants.NO_PITCH_BEND) {
            int pitchBend = this.pitchBend;
            this.pitchBend = -1;
            this.setPitchBend(pitchBend);
        }

        //TODO this results in some weird effects, try to only reapply the ones that actually need it
        //this.controllers.values().forEach(MIDIController::reapply);

        if (this.muted) {
            this.muted = false;
            this.setMuted(true);
        }

        return oldTrack;
    }

    public void setTrack(MIDITrack midiTrack) {
        this.midiTrack = midiTrack;
    }

    public MIDITrack getTrack() {
        return this.midiTrack;
    }

    public boolean isPercussion() {
        return percussion;
    }

    protected int getChannelNum() {
        return channelNum;
    }

    protected void addMessage(MidiMessage message, boolean overrideMute) {
        if (overrideMute || !muted)
            this.midiTrack.add(new MidiEvent(message, getStateTime()));
    }

    protected void addEvent(MidiEvent event, boolean overrideMute) {
        if (overrideMute || !muted)
            this.midiTrack.add(event);
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
        this.dynamic = this.dynamic.louder(velocityMod);
        return this.dynamic;
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

    public int getTranspose() {
        return transposeShift;
    }

    public void setTranspose(int numSemiTones) {
        this.transposeShift = numSemiTones;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        if (!this.muted && muted) {
            //We need to immediately silence the channel as we are making
            //the transition from un muted to muted
            try {
                addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, getChannelNum(), GeneralMidiConstants.ALL_SOUNDS_OFF_CC, 0), true);
            } catch (InvalidMidiDataException e) {
                throw new MidiRuntimeException("Cannot make control change " + GeneralMidiConstants.ALL_SOUNDS_OFF_CC, e);
            }
        }
        this.muted = muted;
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
    public <T extends MIDIController> T getController(MIDIControl<T> controlType) {
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
    public final void finalizeEOT(Beat padding) {
        addEvent(new MidiEvent(Compiler.EOT_MESSAGE, this.stateTime + this.timingEnvironment.ticksInBeat(padding)), true);
    }

    public final void setPitchBend(int bendAmt) {
        // TODO if (this.pitchBend == bendAmt) return;
        ShortMessage resetMessage;
        try {
            resetMessage = new ShortMessage(ShortMessage.PITCH_BEND, channelNum, 0x7F & bendAmt, 0x7F & (bendAmt >> 7));
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set pitch bend to " + bendAmt + ".", e);
        }
        addMessage(resetMessage, true);
        this.pitchBend = bendAmt;
    }

    public final void resetPitchBend() {
        setPitchBend(GeneralMidiConstants.NO_PITCH_BEND);
    }

    public void setSoundBank(int soundBank) {
        // TODO if (this.soundBank == soundBank) return;

        //Preform the sound bank change sequence described in the
        //[General MIDI Constants](../midi/GeneralMidiConstants.html).
        try {
            addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channelNum, GeneralMidiConstants.BANK_SELECT_CC_1, GeneralMidiConstants.BANK_SELECT_CC_1_VAL), true);
            addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, channelNum, GeneralMidiConstants.BANK_SELECT_CC_2, soundBank), true);
            this.soundBank = soundBank;
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set sound bank to " + soundBank + ".", e);
        }
    }

    public void setInstrument(int instrument) {
        // TODO if (this.instrument == instrument) return;

        //Add the program change message
        try {
            addMessage(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channelNum, instrument, 0), true);
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
        Pitch toPlay = pitch.shiftOctave(this.getOctaveShift()).transpose(this.getTranspose());

        if (this.isSlurred() && this.noteStates.get(toPlay).isSlurred) {
            MidiEvent offEvent = noteOffEvents.get(toPlay);
            // TODO this should make sure that the off event corresponds to the last note played, 1 doesn't properly capture this
            if (offEvent != null && (this.getStateTime() - offEvent.getTick()) < 1) {
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
        addMessage(message, false);

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
        addEvent(offEvent, true);
        this.noteOffEvents.set(pitch, offEvent);

        this.noteStates.set(pitch, NoteState.getState(false, isSlurred()));
    }
}
