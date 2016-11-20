package cas.cs4tb3.mellowd.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

public class Pedal {
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

        try {
            this.midiChannel.addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, midiChannel.getChannelNum(), type.getControlNumber(), GeneralMidiConstants.CONTROLLER_VAL_ON), true);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn " + type.getName() + " pedal (cc=" + type.getControlNumber() + ") to on.", e);
        }
    }

    public void release() {
        if (!isPressed) return;
        this.isPressed = false;

        try {
            this.midiChannel.addMessage(new ShortMessage(ShortMessage.CONTROL_CHANGE, midiChannel.getChannelNum(), type.getControlNumber(), GeneralMidiConstants.CONTROLLER_VAL_OFF), true);
        } catch (InvalidMidiDataException e) {
            throw new MidiRuntimeException("Cannot turn " + type.getName() + " pedal (cc=" + type.getControlNumber() + ") to on.", e);
        }
    }

}
