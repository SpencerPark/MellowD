//General Midi Constants
//======================

package org.mellowd.midi;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

//This class will house various MIDI constants for reference throughout the compiler. The
//MIDI protocol has many constants that are standard despite seeming out of place and so
//it is important for the meaning behind the various numbers to be explicitly stated.
public class GeneralMidiConstants {

    //In GM1 standard there are 16 MIDI channels. Channel 10 (index 9) is the channel
    //set aside for percussion and will sound different than the regular channels in that
    //notes played on the drum channels are sound mappings for percussion noises and not
    //pitches.
    public static final Set<Integer> REGULAR_CHANNELS;
    public static final Set<Integer> DRUM_CHANNELS;

    static {
        //Firstly build the regular channel set
        Set<Integer> regChannels = new HashSet<>();
        for (int i = 0; i < 10; i++)
            regChannels.add(i);
        for (int i = 11; i < 16; i++)
            regChannels.add(i);

        //Then wrap channel 9 in a set because in other standards the drum channels
        //span multiple channels and so the compiler is setup to take more than one.
        Set<Integer> drumChannels = new HashSet<>();
        drumChannels.add(9);

        //Lastly wrap the exposed fields in unmodifiable sets and assign them to
        //their respective fields.
        REGULAR_CHANNELS = Collections.unmodifiableSet(regChannels);
        DRUM_CHANNELS = Collections.unmodifiableSet(drumChannels);
    }

    //The pitch bend controller value that resets the pitch bend. This value
    //sets the bend to no bend.
    public static final int NO_PITCH_BEND = 8192;

    //In GM2 a standard controller message was assigned for switching the sound bank
    //to support more instruments. The first message is a controller change with controller
    //`BANK_SELECT_CC_1` and value `BANK_SELECT_CC_1_VAL`. This is followed by a controller
    //change for controller `BANK_SELECT_CC_2` with a value specifying the bank number.

    //Following the bank switch a regular instrument change message will select an instrument
    //from that bank.
    public static final int BANK_SELECT_CC_1 = 0;
    public static final int BANK_SELECT_CC_1_VAL = 121;
    public static final int BANK_SELECT_CC_2 = 32;

    public static final int DEFAULT_SOUND_BANK = 0;

    public static final int SUSTAIN_SWITCH_CC = 64;
    public static final int PORTAMENTO_SWITCH_CC = 65;
    public static final int SOSTENUTO_SWITCH_CC = 66;
    public static final int SOFT_SWITCH_CC = 67;
    public static final int LEGATO_SWITCH_CC = 68;
    public static final int HOLD_SWITCH_CC = 69;
    /**
     * This one works slightly differently. It doesn't make any state
     * changes to the channel. Just shuts all sounds off immediately.
     *
     * Uses a value byte of 0
     */
    public static final int ALL_SOUNDS_OFF_CC = 120;

    public static final int CONTROLLER_VAL_ON = 127;
    public static final int CONTROLLER_VAL_OFF = 0;

    /* public static final int PORTAMENTO_AMOUNT_KNOB_CC = 84;
    *  this is control sets a fake note from which to bend from
    */
    public static final int PORTAMENTO_TIME_KNOB_CC = 5;
    public static final int CHANNEL_VOLUME_KNOB_CC = 7;
    public static final int CHANNEL_PAN_KNOB_CC = 10;
    public static final int CHANNEL_BALANCE_KNOB_CC = 8;
    public static final int SOUND_VARIATION_KNOB_CC = 70;
    public static final int RESONANCE_KNOB_CC = 71;
    public static final int RELEASE_TIME_KNOB_CC = 72;
    public static final int ATTACK_TIME_KNOB_CC = 73;
    public static final int REVERB_KNOB_CC = 91;
    public static final int TREMELO_KNOB_CC = 92;
    public static final int CHORUS_KNOB_CC = 93;
    public static final int DETUNE_KNOB_CC = 94;
    public static final int PHASER_KNOB_CC = 95;

    public static final int KNOB_MIN = 0;
    public static final int KNOB_MAX = 127;

    public static final int MAX_VELOCITY = 127;
    public static final int MIN_VELOCITY = 0;
}
