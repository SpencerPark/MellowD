//Block Options
//============
package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;
import org.antlr.v4.runtime.Token;

//Each block supports various options and this class keeps track of them all in the same place.
//It is strictly a data class and does not perform any functions.
public class BlockOptions {

    //The soundbank is the number describing the patch location in the synthesizer. It
    //most likely will not be used but is available for custom instrument additions.
    private int soundbank;

    //The instrument is the id of the instrument. It does not guarantee the desired instrument selection
    //because each synthesizer is free to change the instrument patches but there does exist a standard
    //instrument set that is wrapped by the [GeneralMidiInstrument](../midi/GeneralMidiInstrument.html)
    //class.
    private int instrument;

    //Percussion blocks accept [GeneralMidiPercussion](../midi/GeneralMidiPercussion.html) as identifiers
    //and need to go on their own channel so the programmer must mark the block as a percussion block. The
    //value of that flag is stored here.
    private boolean percussion;

    //The loop option specifies how many times to repeat the single block that the option is turned on for.
    //If the options are copied the loop value is reset to 0 so that it remains only in the declared block.
    //A loop value less than or equal to 0 implies no repetitions.
    private int loop;

    //The octave is used to resolve pitch or chord descriptions during compilation. It specifies the base
    //octave that all relative pitches are derived from.
    private int octave;

    //The selected channel is an optional parameter with the value -1 if the value is not specified. A
    //selected channel forces the track managers hand in selecting a channel. This is not usually used
    //as most midi details are hidden but it allows full control for the programmer if needed.
    private int selectedChannel = -1;

    //The share channel is a work around for specifying a channel. We want the programmer to help the
    //track manager select a channel for the block but this option allows additional selection constraints
    //without exposing the midi channel id. Channels that use the same instrument may share a channel if
    //we run out. Otherwise the 2 will exist on separate channels.
    private String shareChannel;

    //There are 3 constructors offered by this class. The first being a direct parameter to field assignment
    //allowing the caller to directly specify the default values.
    public BlockOptions(int soundbank, int instrument, boolean percussion, int loop, int octave) {
        this.soundbank = soundbank;
        this.instrument = instrument;
        this.percussion = percussion;
        this.loop = loop;
        this.octave = octave;
    }

    //The second constructor is a copy constructor that copies the data from the passed in constructor
    //to the new instance. As mentioned in the loop field description this copy reset the loop
    //field as it is fragment specific.
    public BlockOptions(BlockOptions options) {
        this.instrument = options.instrument;
        this.percussion = options.percussion;
        this.loop = 0;
        this.octave = options.octave;
        this.soundbank = options.soundbank;
    }

    //The last constructor is a default constructor that sets base values for all of the fields.
    public BlockOptions() {
        this.instrument = GeneralMidiInstrument.ACOUSTIC_GRAND_PIANO.midiNum();
        this.percussion = false;
        this.loop = 0;
        this.soundbank = 0;
        this.octave = 4;
    }

    //The following methods are a collection of getter and setters for manipulating the
    //various properties.
    public void setInstrument(Token name) {
        GeneralMidiInstrument instrument = GeneralMidiInstrument.lookup(name.getText());
        if (instrument == null)
            throw new UndefinedReferenceException(name, "Unknown instrument "+name.getText());
        this.instrument = instrument.midiNum();
    }

    public void setInstrument(int instrument) {
        this.instrument = instrument;
    }

    public void setSoundbank(int soundbank) {
        this.soundbank = soundbank;
    }

    public int getSoundbank() {
        return soundbank;
    }

    public int getInstrument() {
        return instrument;
    }

    public boolean isPercussion() {
        return this.percussion;
    }

    public void setPercussion(boolean percussion) {
        this.percussion = percussion;
    }

    public int getLoopCount() {
        return this.loop;
    }

    public void setLoopCount(int loopCount) {
        this.loop = loopCount;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

    public void setChannel(int channel) {
        this.selectedChannel = channel;
    }

    public boolean isChannelSelected() {
        return this.selectedChannel != -1;
    }

    public int getSelectedChannel() {
        return this.selectedChannel;
    }

    public void setSelectedChannel(int selectedChannel) {
        this.selectedChannel = selectedChannel;
    }

    public String getShareChannel() {
        return shareChannel;
    }

    public void setShareChannel(String shareChannel) {
        this.shareChannel = shareChannel;
    }
}
