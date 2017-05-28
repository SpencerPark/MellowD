//General MIDI Percussion
//=======================

package org.mellowd.midi;

import org.mellowd.primitives.Pitch;

import java.util.HashMap;
import java.util.Map;

//This class is a mapping of names to the General MIDI standard percussion instrument
//id. See [General MIDI Instruments](GeneralMidiInstrument.html) for the General MIDI
//instrument mapping.
public enum GeneralMidiPercussion {
    BASS_DRUM_2(35, "bass2"),
    BASS_DRUM_1(36, "bass", "bass1"),
    RIMSHOT(37, "sideStick", "rim"),
    SNARE_DRUM_1(38, "snare", "snare1"),
    HAND_CLAP(39, "clap"),
    SNARE_DRUM_2(40, "snare2"),
    LOW_TOM_2(41, "ltom2"),
    CLOSED_HI_HAT(42, "cHat", "closedHat", "clHat"),
    LOW_TOM_1(43, "ltom", "ltom1"),
    PEDAL_HI_HAT(44, "pHat", "pedalHat"),
    MID_TOM_2(45, "mtom2"),
    OPEN_HI_HAT(46, "oHat", "openHat"),
    MID_TOM_1(47, "mtom", "mtom1"),
    HIGH_TOM_2(48, "htom2"),
    CRASH_CYMBAL_1(49, "crash", "crash1"),
    HIGH_TOM_1(50, "htom", "htom1"),
    RIDE_CYMBAL_1(51, "ride", "ride1"),
    CHINESE_CYMBAL(52, "trash"),
    RIDE_BELL(53),
    TAMBOURINE(54),
    SPLASH_CYMBAL(55, "splash"),
    COWBELL(56),
    CRASH_CYMBAL_2(57, "crash2"),
    VIBRA_SLAP(58, "vibra"),
    RIDE_CYMBAL_2(59, "ride2"),
    HIGH_BONGO(60, "hbongo"),
    LOW_BONGO(61, "lbongo"),
    MUTE_HIGH_CONGA(62, "mhconga", "mutehconga"),
    OPEN_HIGH_CONGA(63, "hconga", "openhconga"),
    LOW_CONGA(64, "lconga"),
    HIGH_TIMBALE(65, "htim"),
    LOW_TIMBALE(66, "ltim"),
    HIGH_AGOGO(67, "hAgogo"),
    LOW_AGOGO(68, "lAgogo"),
    CABASA(69),
    MARACAS(70),
    SHORT_WHISTLE(71, "sWhistle"),
    LONG_WHISTLE(72, "lWhistle"),
    SHORT_GUIRO(73, "sGuiro"),
    LONG_GUIRO(74, "lGuiro"),
    CLAVES(75, "sticks"),
    HIGH_WOOD_BLOCK(76, "hWoodBlock", "hBlock"),
    LOW_WOOD_BLOCK(77, "lWoodBlock", "lBlock"),
    MUTE_CUICA(78, "mCucia"),
    OPEN_CUICA(79, "oCucia"),
    MUTE_TRIANGLE(80, "mTriangle", "mTri"),
    OPEN_TRIANGLE(81, "oTriangle", "oTri", "triangle", "tri");

    //The only data that the percussion instrument needs is it's midi number. The other
    //information passed into the constructor is meta data so the instance can register itself
    //in the lookup table.
    private final int midiNum;

    GeneralMidiPercussion(int midiNum, String... lookupKeys) {
        this.midiNum = midiNum;
        registerLookup(this, lookupKeys);
    }

    public int midiNum() {
        return midiNum;
    }

    //In MIDI the percussion sounds are simply notes that are played on a designated channel.
    //In GM1 this channel is channel 10 (index 9). When you play the note 35 for example you will
    //hear the bass2 sound. Therefor to easily build percussion into the compiler's infrastructure
    //we can convert the drum name to a pitch and treat it like all other sounds.
    public Pitch getAsPitch() {
        return Pitch.getPitch(midiNum);
    }

    @Override
    public String toString() {
        return name() + ":" + midiNum;
    }

    //The name lookup is an important service of this class. We will map all of the various
    //names to its respectful percussion sound.

    private static Map<String, GeneralMidiPercussion> lookupByName;

    private static void registerLookup(GeneralMidiPercussion instrument, String... lookupKeys) {
        if (lookupByName == null) lookupByName = new HashMap<>();
        //Add the instrument name (excluding underscores `_`) as a key to the `instrument`
        lookupByName.put(instrument.name().toLowerCase().replace("_", ""), instrument);
        //Add all of the specified keys as keys to the `instrument`
        for (String key : lookupKeys) {
            lookupByName.put(key.toLowerCase(), instrument);
        }
    }

    //Lookup a `GeneralMidiPercussion` instrument by name. This lookup is case insensitive
    //and ignores underscores (`_`) and spaces.
    public static GeneralMidiPercussion lookup(String key) {
        return lookupByName.get(key.toLowerCase().replace("_", "").replace(" ", ""));
    }
}
