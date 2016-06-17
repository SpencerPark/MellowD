package cas.cs4tb3.mellowd.midi;

import java.util.function.BiFunction;

public final class MIDIControl<T> {
    public static final MIDIControl<Knob>  PORTAMENTO_TIME  = new MIDIControl<>("PORTAMENTO_TIME", GeneralMidiConstants.PORTAMENTO_TIME_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  VOLUME           = new MIDIControl<>("VOLUME", GeneralMidiConstants.CHANNEL_VOLUME_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  ATTACK_TIME      = new MIDIControl<>("ATTACK_TIME", GeneralMidiConstants.ATTACK_TIME_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  REVERB           = new MIDIControl<>("REVERB", GeneralMidiConstants.REVERB_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  TREMELO          = new MIDIControl<>("TREMELO", GeneralMidiConstants.TREMELO_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  CHORUS           = new MIDIControl<>("CHORUS", GeneralMidiConstants.CHORUS_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  DETUNE           = new MIDIControl<>("DETUNE", GeneralMidiConstants.DETUNE_KNOB_CC, Knob::new);
    public static final MIDIControl<Knob>  PHASER           = new MIDIControl<>("PHASER", GeneralMidiConstants.PHASER_KNOB_CC, Knob::new);

    public static final MIDIControl<Pedal> SUSTAIN          = new MIDIControl<>("SUSTAIN", GeneralMidiConstants.SUSTAIN_SWITCH_CC, Pedal::new);
    public static final MIDIControl<Pedal> PORTAMENTO       = new MIDIControl<>("PORTAMENTO", GeneralMidiConstants.PORTAMENTO_SWITCH_CC, Pedal::new);
    public static final MIDIControl<Pedal> SOSTENUTO        = new MIDIControl<>("SOSTENUTO", GeneralMidiConstants.SOSTENUTO_SWTICH_CC, Pedal::new);
    public static final MIDIControl<Pedal> LEGATO           = new MIDIControl<>("LEGATO", GeneralMidiConstants.LEGATO_SWITCH_CC, Pedal::new);

    private final String name;
    private final int controlNumber;
    private final BiFunction<MIDIChannel, MIDIControl, T> instanceCreator;

    private MIDIControl(String name, int controlNumber, BiFunction<MIDIChannel, MIDIControl, T> instanceCreator) {
        this.name = name;
        this.controlNumber = controlNumber;
        this.instanceCreator = instanceCreator;
    }

    public String getName() {
        return name;
    }

    public int getControlNumber() {
        return controlNumber;
    }

    public T attachTo(MIDIChannel channel) {
        return instanceCreator.apply(channel, this);
    }
}
