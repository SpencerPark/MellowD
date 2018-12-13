package org.mellowd.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class Pedal implements MIDIController {
    private final MIDIChannel midiChannel;
    private final MIDIControl<Pedal> type;
    private boolean isPressed = false;

    public Pedal(MIDIChannel midiChannel, MIDIControl<Pedal> type) {
        this.midiChannel = midiChannel;
        this.type = type;
    }

    public MIDIControl<Pedal> getControlType() {
        return type;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void press() {
        if (isPressed) return;
        this.isPressed = true;

        this.addMessage();
    }

    public void release() {
        if (!isPressed) return;
        this.isPressed = false;

        this.addMessage();
    }

    private void addMessage() {
        try {
            ShortMessage message = new ShortMessage(ShortMessage.CONTROL_CHANGE, midiChannel.getChannelNum(), type.getControlNumber(), this.isPressed ? GeneralMidiConstants.CONTROLLER_VAL_ON : GeneralMidiConstants.CONTROLLER_VAL_OFF);
            this.midiChannel.addMessage(message, true);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn " + type.getName() + " pedal (cc=" + type.getControlNumber() + ") to " + (this.isPressed ? "on" : "off") + ".", e);
        }
    }

    @Override
    public void reapply() {
        this.addMessage();
    }
}
