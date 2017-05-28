//General MIDI Instruments
//========================
package org.mellowd.midi;

import java.util.HashMap;
import java.util.Map;

//This class is a mapping of names to the General MIDI standard instrument
//id. See [General MIDI Percussion](GeneralMidiPercussion.html) for the General MIDI
//percussion instrument mapping.
public enum GeneralMidiInstrument {
    //Piano
    ACOUSTIC_GRAND_PIANO(1, "acousticGrand", "acousticPiano", "piano"),
    BRIGHT_ACOUSTIC_PIANO(2, "brightPiano"),
    ELECTRIC_GRAND_PIANO(3, "electricGrand"),
    HONKY_TONK_PIANO(4, "tackPiano", "tinnyPiano"),
    ELECTRIC_PIANO_1(5, "electricPiano"),
    ELECTRIC_PIANO_2(6),
    HARPSICHORD(7),
    CLAVINET(8),

    //Chromatic Percussion
    CELESTA(9),
    GLOCKENSPIEL(10, "metalXylophone"),
    MUSIC_BOX(11),
    VIBRAPHONE(12, "vibraharp", "vibes"),
    MARIMBA(13),
    XYLOPHONE(14, "woodXylophone"),
    TUBULAR_BELLS(15, "chimes", "bells"),
    DULCIMER(16),

    //Organ
    DRAWBAR_ORGAN(17, "hammondOrgan", "hammond"),
    PERCUSSIVE_ORGAN(18, "percussiveHammondOrgan", "percussiveHammond"),
    ROCK_ORGAN(19, "electricOrgan"),
    CHURCH_ORGAN(20, "organ", "pipeOrgan"),
    REED_ORGAN(21, "pumpOrgan"),
    ACCORDION(22),
    HARMONICA(23),
    TANGO_ACCORDION(24, "bandoneon"),

    //Guitar
    ACOUSTIC_GUITAR_NYLON(25, "nylonGuitar", "classicalGuitar"),
    ACOUSTIC_GUITAR_STEEL(26, "acousticGuitar", "steelGuitar"),
    ELECTRIC_GUITAR_JAZZ(27, "jazzGuitar"),
    ELECTRIC_GUITAR_CLEAN(28, "electricGuitar"),
    ELECTRIC_GUITAR_MUTED(29, "mutedElectricGuitar"),
    OVERDRIVEN_GUITAR(30, "overdriveGuitar"),
    DISTORTION_GUITAR(31, "distortedGuitar", "rockGuitar"),
    GUITAR_HARMONICS(32, "harmonicGuitar"),

    //Bass
    ACOUSTIC_BASS(33, "acousticBassGuitar"),
    ELECTRIC_BASS_FINGER(34, "electricBass", "bassGuitar", "electricBassGuitar"),
    ELECTRIC_BASS_PICK(35, "pickBass", "bassGuitarPick", "electricBassGuitarPick"),
    FRETLESS_BASS(36, "mutedBass", "fretlessBassGuitar", "mutedBassGuitar"),
    SLAP_BASS_1(37, "slapBass", "slapBassGuitar", "slapBassGuitar1"),
    SLAP_BASS_2(38, "slapBassGuitar2"),
    SYNTH_BASS_1(39, "synthBass", "synthBassGuitar", "synthBassGuitar1"),
    SYNTH_BASS_2(40, "synthBassGuitar2"),

    //Strings
    VIOLIN(41),
    VIOLA(42),
    CELLO(43),
    CONTRABASS(44, "bassCello"),
    TREMOLO_STRINGS(45, "fastStrings", "jitteryStrings"),
    PIZZICATO_STRINGS(46, "pluckedStrings"),
    ORCHESTRAL_HARP(47, "harp"),
    TIMPANI(48, "kettledrums", "kettledrum"),

    //Ensemble
    STRING_ENSEMBLE_1(49, "stringEnsemble", "strings", "strings1"),
    STRING_ENSEMBLE_2(50, "strings2"),
    SYNTH_STRINGS_1(51, "synthStringEnsemble", "synthStringEnsemble1", "synthStrings"),
    SYNTH_STRINGS_2(52, "synthStringEnsemble2"),
    CHOIR_AAHS(53, "aahs", "ahhs", "ahh", "aah", "choirAhhs", "voiceAahs", "voiceAhhs"),
    VOICE_OOHS(54, "oohs", "ooh", "choirOohs"),
    SYNTH_CHOIR(55, "choir"),
    ORCHESTRA_HIT(56, "orchestralHit"),

    //Brass
    TRUMPET(57),
    TROMBONE(58),
    TUBA(59),
    MUTED_TRUMPET(60),
    FRENCH_HORN(61, "horn"),
    BRASS_SECTION(62),
    SYNTH_BRASS_1(63, "synthBrass"),
    SYNTH_BRASS_2(64),

    //Reed
    SOPRANO_SAX(65, "sopranoSaxophone"),
    ALTO_SAX(66, "altoSaxaphone"),
    TENOR_SAX(67, "tenorSaxophone"),
    BARITONE_SAX(68, "baritoneSaxophone", "bariSax", "bariSaxophone"),
    OBOE(69),
    ENGLISH_HORN(70),
    BASSOON(71),
    CLARINET(72),

    //Pipe
    PICCOLO(73),
    FLUTE(74),
    RECORDER(75),
    PAN_FLUTE(76),
    BLOWN_BOTTLE(77, "jug"),
    SHAKUHACHI(78, "japaneseFlute"),
    WHISTLE(79),
    OCARINA(80, "sweetPotato"),

    //Synth Lead
    LEAD_1(81, "synth", "squareSynth"),
    LEAD_2(82, "sawtoothSynth"),
    LEAD_3(83, "steamWhistle", "trainWhistle", "calliope", "calliopeSynth"),
    LEAD_4(84, "chiff", "chiffSynth"),
    LEAD_5(85, "charango", "charangoSynth"),
    LEAD_6(86, "voiceSynth", "voice"),
    LEAD_7(87, "fifthsSynth", "harmonicSynth"),
    LEAD_8(88, "bassSynth"),

    //Synth Pad
    PAD_1(89, "newAgeSynth"),
    PAD_2(90, "warmSynth"),
    PAD_3(91, "polySynth"),
    PAD_4(92, "choirSynth"),
    PAD_5(93, "bowedSynth"),
    PAD_6(94, "metallicSynth", "metalSynth"),
    PAD_7(95, "haloSynth"),
    PAD_8(96, "sweepSynth"),

    //Synth Effects
    FX_1(97, "rainSynth"),
    FX_2(98, "soundtrackSynth"),
    FX_3(99, "crystalSynth"),
    FX_4(100, "atmosphereSynth"),
    FX_5(101, "brightSynth"),
    FX_6(102, "goblinSynth"),
    FX_7(103, "echoSynth"),
    FX_8(104, "scifiSynth"),

    //Ethnic
    SITAR(105),
    BANJO(106),
    SHAMISEN(107, "samisen"),
    KOTO(108),
    KALIMBA(109, "marimbula"),
    BAGPIPE(110, "bagpipes"),
    FIDDLE(111),
    SHANAI(112, "shehnai", "shahnai", "mangalvadya"),

    //Percussive
    TINKLE_BELL(113, "smallBell"),
    AGOGO(114, "gangan"),
    STEEL_DRUM(115, "steeldrums"),
    WOODBLOCK(116, "clogbox"),
    TAIKO_DRUM(117, "taiko"),
    MELODIC_TOM(118),
    SYNTH_DRUM(119),
    REVERSE_CYMBAL(120),

    //Sound Effects
    GUITAR_FRET(121, "fretSlide"),
    BREATH_NOISE(122),
    SEASHORE(123, "waves", "ocean"),
    BIRD_TWEET(124, "bird", "tweet", "chirp"),
    TELEPHONE_RING(125, "phone", "telephone"),
    HELICOPTER(126),
    APPLAUSE(127),
    GUNSHOT(128);

    //The only data that the percussion instrument needs is it's midi number. The other
    //information passed into the constructor is meta data so the instance can register itself
    //in the lookup table.
    private final int midiNum;

    GeneralMidiInstrument(int midiNum, String... lookupKeys) {
        this.midiNum = midiNum-1;
        registerLookup(this, lookupKeys);
    }

    public int midiNum() {
        return midiNum;
    }

    @Override
    public String toString() {
        return name() + ":" + midiNum;
    }

    //The name lookup is an important service of this class. We will map all of the various
    //names to its respectful instrument.

    private static Map<String, GeneralMidiInstrument> lookupByName;

    private static void registerLookup(GeneralMidiInstrument instrument, String... lookupKeys) {
        if (lookupByName == null) lookupByName = new HashMap<>();
        //Add the instrument name (excluding underscores `_`) as a key to the `instrument`
        lookupByName.put(instrument.name().toLowerCase().replace("_", ""), instrument);
        //Add all of the specified keys as keys to the `instrument`
        for (String key : lookupKeys) {
            lookupByName.put(key.toLowerCase(), instrument);
        }
    }

    //Lookup a `GeneralMidiInstrument` by name. This lookup is case insensitive
    //and ignores underscores (`_`) and spaces.
    public static GeneralMidiInstrument lookup(String key) {
        return lookupByName.get(key.toLowerCase().replace("_", "").replace(" ", ""));
    }
}
