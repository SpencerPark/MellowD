package cas.cs4tb3.mellowd.midi;

import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.compiler.Compiler;
import cas.cs4tb3.mellowd.primitives.Beat;
import cas.cs4tb3.mellowd.primitives.Dynamic;
import cas.cs4tb3.mellowd.primitives.Pitch;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.*;

//A MIDIChannel wraps a javax.sound.midi.Track to provide virtual state information.
public class MIDIChannel {
    private static class ScheduledAction {
        Runnable action;
        long stateTime;

        public ScheduledAction(Runnable action, long stateTime) {
            this.action = action;
            this.stateTime = stateTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduledAction that = (ScheduledAction) o;

            if (stateTime != that.stateTime) return false;
            return action.equals(that.action);

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

        public static int compare(ScheduledAction o1, ScheduledAction o2) {
            return Long.compare(o1.stateTime, o2.stateTime);
        }
    }

    public static final int DEFAULT_OFF_VELOCITY = 96;

    private final Track midiTrack;
    private final boolean isPercussion;
    private final int channelNum;
    private final TimingEnvironment timingEnvironment;
    private final BitSet notesOn;
    private final SortedSet<ScheduledAction> scheduledActions;

    private int instrument = GeneralMidiInstrument.ACOUSTIC_GRAND_PIANO.midiNum();
    private int soundBank = GeneralMidiConstants.DEFAULT_SOUND_BANK;
    private Dynamic dynamic = Dynamic.mf; //The dynamic is mf by default
    private long stateTime = 0L;
    private int pitchBend = GeneralMidiConstants.NO_PITCH_BEND;
    private final Map<MIDIControl<?>, Object> controllers;
    private int octaveShift = 0;

    //This is a counter keeping track of the times that the slurred flag has been set
    //so that 2 calls to slurred require another 2 calls to un-slur.
    private int slurred = 0;

    public MIDIChannel(Track midiTrack, boolean isPercussion, int channelNum, TimingEnvironment timingEnvironment) {
        this.midiTrack = midiTrack;
        this.isPercussion = isPercussion;
        this.channelNum = channelNum;
        this.timingEnvironment = timingEnvironment;
        this.notesOn = new BitSet(128);
        this.scheduledActions = new TreeSet<>(ScheduledAction::compare);
        this.controllers = new HashMap<>();
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

        //Preform all of the scheduled actions up until the new current time
        Iterator<ScheduledAction> actionIterator = this.scheduledActions.iterator();
        while (actionIterator.hasNext()) {
            ScheduledAction next = actionIterator.next();
            if (next.stateTime <= newTime) {
                //We found an action that we need to preform now so
                //pull it from the list and execute it.
                actionIterator.remove();
                this.stateTime = next.stateTime;
                next.action.run();
            } else {
                //We ran into a scheduled action that should happen later and since
                //the list is sorted we know that there will be no more actions to
                //execute.
                break;
            }
        }

        return this.stateTime = newTime;
    }

    public synchronized final long stepIntoFuture(Beat beat) {
        return stepIntoFuture(this.timingEnvironment.ticksInBeat(beat));
    }

    public boolean isNoteOn(Pitch pitch) {
        return this.notesOn.get(pitch.getMidiNum());
    }

    public boolean isNoteOff(Pitch pitch) {
        return !this.notesOn.get(pitch.getMidiNum());
    }

    public boolean isPitchBent() {
        return this.pitchBend != GeneralMidiConstants.NO_PITCH_BEND;
    }

    public boolean isSlurred() {
        return this.slurred > 0;
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

    public boolean setSlurred(boolean slurred) {
        if (slurred) {
            this.slurred++;
        } else {
            this.slurred = Math.max(0, this.slurred - 1);
        }
        return this.slurred > 0;
    }

    public final void doLater(Beat timeOffset, Runnable action) {
        long stateTimeOffset = this.timingEnvironment.ticksInBeat(timeOffset);
        doLater(stateTimeOffset, action);
    }

    public final void doLater(long stateTimeOffset, Runnable action) {
        if (stateTimeOffset <= 0) {
            action.run();
            return;
        }

        ScheduledAction scheduledAction = new ScheduledAction(action, this.stateTime + stateTimeOffset);
        this.scheduledActions.add(scheduledAction);
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
            throw new MidiRuntimeException("Cannot set pitch bend to "+bendAmt+".", e);
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
            throw new MidiRuntimeException("Cannot set sound bank to "+soundBank+".", e);
        }
    }

    private void setInstrument(int instrument) {
        if (this.instrument == instrument) return;

        //Add the program change message
        try {
            this.midiTrack.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channelNum, instrument, 0), stateTime));
            this.instrument = instrument;
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot set the instrument to "+instrument+".", e);
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

    private Pitch adjustToChannelSettings(Pitch toPlay) {
        if (toPlay == Pitch.REST) return toPlay;
        return toPlay.shiftOctave(this.getOctaveShift());
    }

    public final void noteOn(Pitch pitch, int velocityMod) {
        pitch = adjustToChannelSettings(pitch);

        if (pitch == Pitch.REST) return;

        ShortMessage message;
        try {
            message = new ShortMessage(ShortMessage.NOTE_ON, channelNum, pitch.getMidiNum(), this.dynamic.louder(velocityMod).getVelocity());
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn note on ("+pitch.getMidiNum()+") with dynamic of "+this.dynamic.louder(velocityMod).getVelocity()+".", e);
        }
        this.midiTrack.add(new MidiEvent(message, this.stateTime));

        //Track the note as on.
        this.notesOn.set(pitch.getMidiNum(), true);
    }

    public final void noteOff(Pitch pitch, int offVelocity) {
        pitch = adjustToChannelSettings(pitch);

        if (pitch == Pitch.REST) return;

        ShortMessage message;
        try {
            message = new ShortMessage(ShortMessage.NOTE_OFF, channelNum, pitch.getMidiNum(), offVelocity);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn note off ("+pitch.getMidiNum()+") with dynamic of "+offVelocity+".", e);
        }

        this.midiTrack.add(new MidiEvent(message, this.stateTime));

        //Track the note as off.
        this.notesOn.set(pitch.getMidiNum(), false);
    }

    public final void noteOn(Pitch pitch) {
        noteOn(pitch, 0);
    }

    public final void noteOff(Pitch pitch) {
        noteOff(pitch, DEFAULT_OFF_VELOCITY);
    }
}
