package org.mellowd.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class Knob implements MIDIController {
    private final MIDIControl<Knob> type;
    private final MIDIChannel midiChannel;
    private int setting = -1;

    public Knob(MIDIChannel midiChannel, MIDIControl<Knob> type) {
        this.midiChannel = midiChannel;
        this.type = type;
    }

    public MIDIControl<Knob> getControlType() {
        return type;
    }

    public int currentSetting() {
        return this.setting != -1 ? this.setting : 0;
    }

    @Override
    public void reapply() {
        this.addMessage();
    }

    public void twist(int newSetting) {
        newSetting = clipSetting(newSetting);
        if (newSetting == this.setting) return;

        this.setting = newSetting;
        this.addMessage();
    }

    private void addMessage() {
        try {
            this.midiChannel.addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, midiChannel.getChannelNum(), type.getControlNumber(), setting), true);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot twist " + type.getName() + " knob (cc=" + type.getControlNumber() + ") to " + setting + ".", e);
        }
    }

    private static int clipSetting(int setting) {
        return Math.max(GeneralMidiConstants.KNOB_MIN, Math.min(setting, GeneralMidiConstants.KNOB_MAX));
    }
}
