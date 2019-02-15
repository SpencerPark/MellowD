package org.mellowd.midi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class MIDIControl<T extends MIDIController> {
    private static final Map<String, MIDIControl> lookupByName = new HashMap<>();

    public static final MIDIControl<Knob>  PORTAMENTO_TIME  = new MIDIControl<>("PORTAMENTO_TIME", GeneralMidiConstants.PORTAMENTO_TIME_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  VOLUME           = new MIDIControl<>("VOLUME", GeneralMidiConstants.CHANNEL_VOLUME_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  PAN              = new MIDIControl<>("PAN", GeneralMidiConstants.CHANNEL_PAN_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  BALANCE          = new MIDIControl<>("BALANCE", GeneralMidiConstants.CHANNEL_BALANCE_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  SOUND_VARIATION  = new MIDIControl<>("SOUND_VARIATION", GeneralMidiConstants.SOUND_VARIATION_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  RESONANCE        = new MIDIControl<>("RESONANCE", GeneralMidiConstants.RESONANCE_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  RELEASE_TIME     = new MIDIControl<>("RELEASE_TIME", GeneralMidiConstants.RELEASE_TIME_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  ATTACK_TIME      = new MIDIControl<>("ATTACK_TIME", GeneralMidiConstants.ATTACK_TIME_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  REVERB           = new MIDIControl<>("REVERB", GeneralMidiConstants.REVERB_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  TREMELO          = new MIDIControl<>("TREMELO", GeneralMidiConstants.TREMELO_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  CHORUS           = new MIDIControl<>("CHORUS", GeneralMidiConstants.CHORUS_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  DETUNE           = new MIDIControl<>("DETUNE", GeneralMidiConstants.DETUNE_KNOB_CC, Knob.class, Knob::new);
    public static final MIDIControl<Knob>  PHASER           = new MIDIControl<>("PHASER", GeneralMidiConstants.PHASER_KNOB_CC, Knob.class, Knob::new);

    public static final MIDIControl<Pedal> SUSTAIN          = new MIDIControl<>("SUSTAIN", GeneralMidiConstants.SUSTAIN_SWITCH_CC, Pedal.class, Pedal::new);
    public static final MIDIControl<Pedal> PORTAMENTO       = new MIDIControl<>("PORTAMENTO", GeneralMidiConstants.PORTAMENTO_SWITCH_CC, Pedal.class, Pedal::new);
    public static final MIDIControl<Pedal> SOSTENUTO        = new MIDIControl<>("SOSTENUTO", GeneralMidiConstants.SOSTENUTO_SWITCH_CC, Pedal.class, Pedal::new);
    public static final MIDIControl<Pedal> SOFT             = new MIDIControl<>("SOFT", GeneralMidiConstants.SOFT_SWITCH_CC, Pedal.class, Pedal::new);
    public static final MIDIControl<Pedal> LEGATO           = new MIDIControl<>("LEGATO", GeneralMidiConstants.LEGATO_SWITCH_CC, Pedal.class, Pedal::new);
    public static final MIDIControl<Pedal> HOLD             = new MIDIControl<>("HOLD", GeneralMidiConstants.HOLD_SWITCH_CC, Pedal.class, Pedal::new);

    private final String name;
    private final int controlNumber;
    private final Class<T> controllerType;
    private final BiFunction<MIDIChannel, MIDIControl<T>, T> instanceCreator;

    private MIDIControl(String name, int controlNumber, Class<T> controllerType, BiFunction<MIDIChannel, MIDIControl<T>, T> instanceCreator) {
        this.name = name;
        this.controlNumber = controlNumber;
        this.controllerType = controllerType;
        this.instanceCreator = instanceCreator;

        lookupByName.put(name.toLowerCase().replace("_", ""), this);
    }

    public String getName() {
        return name;
    }

    public int getControlNumber() {
        return controlNumber;
    }

    public Class<T> getControllerType() {
        return controllerType;
    }

    public T attachTo(MIDIChannel channel) {
        return instanceCreator.apply(channel, this);
    }

    public static MIDIControl<?> getController(String name) {
        return lookupByName.get(name.toLowerCase().replace("_", ""));
    }
}
