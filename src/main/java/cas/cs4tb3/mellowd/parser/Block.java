//Block
//=====

package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.Articulation;
import cas.cs4tb3.mellowd.Dynamic;
import cas.cs4tb3.mellowd.PlayableSound;
import cas.cs4tb3.mellowd.TimingEnvironment;
import cas.cs4tb3.mellowd.midi.GeneralMidiConstants;
import cas.cs4tb3.mellowd.primitives.Chord;
import cas.cs4tb3.mellowd.primitives.Phrase;
import org.antlr.v4.runtime.Token;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.util.*;

//This class is a core element in the compiler. It does a good majority of the actual translation
//to MIDI events. It is responsible for keeping its own time so that it knows where to place events
//when they are received.
public class Block {
    //This declaration specifies the amount to bend the pitch each glissando step
    private static final int BEND_AMT = 128;

    //These are the the longer stateful fields that stay around for
    //a while. They are mostly used for referencing.
    private final TrackManager trackManager;
    private final TimingEnvironment timingEnv;
    private final Track track;
    private BlockOptions options;
    private final String name;

    //These fields are actively changing and are used for temporary holding
    //data between adding phrases and sounds.
    private long time = 0;
    private Dynamic dynamic;
    private List<Phrase> dynBuffer = new LinkedList<>();
    private boolean cres;
    private boolean decres;
    private Token cresToken;

    //This data will help with looped compilation units. In order to append events
    //we need to know at what time the block opened and all the events added since
    //it was opened. This way when the block is closed we can add all of the events
    //again.
    private int loopCount = 0;
    private long passStartTime = 0L;
    private List<MidiEvent> addedThisPass = new LinkedList<>();

    //This message need to be delayed by one note so it needs to be stored somewhere
    //stateful. When a pitch change is made we want to cancel it right before the next
    //note but at the time the note is received the compiler doesn't know when the next
    //note will be so we can hold onto the reset up here.
    private ShortMessage queuedPitchReset;

    //The `lastAddedSlurOffs` is a list of NoteOff messages that have been added but the notes
    //need to be slurred. As a result they may need to be pushed further into the future and
    //so they need to be remembered for atleast one extra note addition.
    private Set<MidiEvent> lastAddedSlurOffs = new HashSet<>();

    private long lastTick = 0L;

    protected Block(TrackManager trackManager, TimingEnvironment timingEnv, Track track, BlockOptions options, String name, Dynamic dynamic) {
        this.trackManager = trackManager;
        this.timingEnv = timingEnv;
        this.track = track;
        this.options = options;
        this.name = name;
        this.dynamic = dynamic;
    }

    public BlockOptions getOptions() {
        return this.options;
    }

    public String getName() {
        return this.name;
    }

    public void setDynamic(Dynamic dynamic) throws InvalidMidiDataException {
        //If the block is currently in a decrescendo but the token we just encountered is louder
        //than the current dynamic the a crescendo is what should have been specified.
        if (this.decres && dynamic.getVelocity() >= this.dynamic.getVelocity())
            throw new ParseException(cresToken, "Decrescendo specified but next volume token (" + dynamic.getVelocity()
                    + ") is the same or louder than the first (" + this.dynamic.getVelocity()
                    + "). Did you mean crescendo?");

        //If the block is currently in a crescendo but the token we just encountered is quieter
        //than the current dynamic the a decrescendo is what should have been specified.
        if (this.cres && dynamic.getVelocity() <= this.dynamic.getVelocity())
            throw new ParseException(cresToken, "Crescendo specified but next volume token (" + dynamic.getVelocity()
                    + ") is the same or quieter than the first (" + this.dynamic.getVelocity()
                    + "). Did you mean decrescendo?");

        //If we are currently is a gradual dynamic change then this means all additions
        //have been queued in the `dynBuffer` and they need to be compiled now that we
        //know the target dynamic.
        if (this.cres || this.decres) {
            //First we need to collect all the sounds from all of the queued phrases
            List<PlayableSound> sounds = new LinkedList<>();
            for (Phrase buffered : this.dynBuffer) {
                Collections.addAll(sounds, buffered.getSounds());
            }
            //Apply the appropriate dynamic to the sounds
            this.dynamic.graduallyApplyToAllSounds(dynamic, this.timingEnv, sounds);
            //Finally add all the sounds to the track
            for (PlayableSound sound : sounds) {
                addSound(sound);
            }
            //Reset everything
            this.cres = false;
            this.decres = false;
            this.dynBuffer.clear();
        }

        //Set the current dynamic state.
        this.dynamic = dynamic;
    }

    //The crescendo and decrescendo methods are used to mark the block
    //as gradually changing velocity. This means that all phrases are held
    //until the target of the change is specified at the end of the phrases
    //that the change is applied to.
    //
    //It requires a token simply for possible error messages later on.
    public void crescendo(Token cresToken) {
        this.cres = true;
        this.decres = false;
        this.cresToken = cresToken;
    }

    public void decrescendo(Token decresToken) {
        this.cres = false;
        this.decres = true;
        this.cresToken = decresToken;
    }

    //This is the entry point for incoming data. Phrases that need to be compiled are
    //passed into this method.
    public void addPhrase(Phrase phrase) throws InvalidMidiDataException {
        //If the block is in a gradual change then the dynamic is not yet known
        //and the phrase should be queued for later.
        if (this.cres || this.decres) {
            //Queue the phrase
            this.dynBuffer.add(phrase);
        } else {
            //We can compile it now because there is no crescendo or decrescendo happening
            //so the dynamic is static.
            addPhraseStaticDynamic(phrase);
        }
    }

    //This method compiles the phrase using the current dynamic for all sounds
    //in the phrase.
    protected void addPhraseStaticDynamic(Phrase phrase) throws InvalidMidiDataException {
        this.dynamic.applyToAllSounds(phrase.getSounds());
        for (PlayableSound sound : phrase.getSounds()) {
            addSound(sound);
        }
    }

    //This method adds loop support for the block. If the block's loop count
    //is positive, meaning that the data compiled in this segment will be repeat, then
    //the event must be saved so it can be repeated when the block closes.

    //The event must also be added to the track.
    private void addEventToTrack(MidiEvent event) {
        if (loopCount > 0) {
            this.addedThisPass.add(event);
        }
        this.track.add(event);
    }

    //Here is where the real compilation happens. This method compiles a single sound
    //including the articulation.
    protected void addSound(PlayableSound sound) throws InvalidMidiDataException {
        //First things first, we need to add the `queuedPitchReset` if one exists
        //so that this sound isn't bend out of shape.
        if (this.queuedPitchReset != null) {
            addEventToTrack(new MidiEvent(this.queuedPitchReset, time-1));
            this.queuedPitchReset = null;
        }

        //The channel is a major part of the MIDI scheduling so it is always left
        //to the [TrackManager](TrackManager.html) to decide on the channel. We will
        //only ask once per sound because changing channels in the middle of playing
        //a sound is not possible.
        TrackManager.Channel channel = this.trackManager.getChannel(this);

        //The plain sound data is retrieved from the sound. This base data will
        //then pass through the articulation modifications to tweak it for a more
        //custom sound.
        long tickDuration = this.timingEnv.ticksInBeat(sound.getDuration());
        boolean slurred = false;
        if (sound.isSlurred()) {
            slurred = true;
            tickDuration += tickDuration/8;
        }
        int velocity = sound.getVelocity();
        int offVelocity = slurred ? 1 : 96;
        boolean roll = false;
        long offsetStep = 0;

        //Apply the articulation effects
        switch (sound.getArticulation()) {
            //Staccato makes the performance short and choppy. Described in jazz
            //as `dit`. To achieve this effect the duration will be chopped to a
            //third of its value and the note will be ended very quickly.
            case STACCATO:
                tickDuration = tickDuration / 3;
                offVelocity = 127;
                break;
            //Staccatissimo makes the performance short but more powerful. It is
            //given some more emphasis. It is similar to staccato but the duration
            //is going to be chopped to a half (rather than a third) and it will be
            //played with a bit more velocity.
            case STACCATISSIMO:
                tickDuration = tickDuration / 2;
                velocity = Dynamic.clip(velocity + 3);
                offVelocity = 127;
                break;
            //Marcato is the same a staccato but with more power. It is referred to
            //as `dhat` by jazz musicians and to preform a note with articulated with marcato
            //the note's duration will be chopped to a third, the velocity will be increased
            //and the note will be release very quickly.
            case MARCATO:
                tickDuration = tickDuration / 3;
                velocity = Dynamic.clip(velocity + 5);
                offVelocity = 127;
                break;
            //An accent is played by attacking the note. This gives it a much faster velocity and
            //will also drop off a bit quicker than the average note. This is sometimes referred to
            //as `dah` by jazz musicians.
            case ACCENT:
                velocity = Dynamic.clip(velocity + 6);
                offVelocity = 113;
                break;
            //Tenuto is the equivalent of a single note slur. It is also called `doo` by jazz musicians
            //and so in order to preform a tenuto note the note will be let off as slow as possible with
            //a slightly longer duration.
            case TENUTO:
                slurred = true;
                tickDuration += (tickDuration / 8);
                offVelocity = 1;
                break;
            //Gliscando is a glide. It can be preformed as a pitch bend. To preform a gliscando a total of
            //16 pitch bend changes will give the effect that the note is falling or climbing (depending
            //on the direction of bend). These changes will be equally spaced over the duration of the note
            //as to not interfere with the next note. Additionally a reset message will be queued for the
            //next note to take.
            case GLISCANDO:
                //If the sound is a chord a gliscando should be preformed as a roll.
                if (sound.isChord()) {
                    roll = true;
                    //The roll will play the first note in the chord right on the down beat. Each
                    //consecutive note in the chord will be delayed by the `offsetStep`.
                    offsetStep = tickDuration / (((Chord) sound.getSound()).size() * 2);
                //otherwise preform the gliscando as a pitch bend.
                } else {
                    int bendAmt = GeneralMidiConstants.NO_PITCH_BEND;
                    int bendTickOffset;
                    for (bendTickOffset = 0; bendTickOffset < tickDuration; bendTickOffset += tickDuration / 16) {
                        bendAmt += BEND_AMT;
                        ShortMessage message = new ShortMessage(ShortMessage.PITCH_BEND, channel.midiChannelNum, 0x7F & bendAmt, 0x7F & (bendAmt >> 7));
                        addEventToTrack(new MidiEvent(message, time + bendTickOffset));
                    }
                    this.queuedPitchReset = new ShortMessage(ShortMessage.PITCH_BEND, channel.midiChannelNum, 0x7F & GeneralMidiConstants.NO_PITCH_BEND, 0x7F & (GeneralMidiConstants.NO_PITCH_BEND >> 7));
                }
                break;
        }

        //Initialize the offset to 0. If `!roll` then this offset will remain at 0. If
        //the `roll` is set to true each time a note is added to the track the next is offset
        //`offsetStep`.
        long offset = 0L;

        //Now that all of the performance data is set we can create the events and add them to the
        //track.
        NoteOnLoop:
        for (ShortMessage message : sound.getSound().noteOn(channel.midiChannelNum, velocity)) {
            if (slurred) {
                for (MidiEvent lastOffEvent : lastAddedSlurOffs) {
                    if (((ShortMessage) lastOffEvent.getMessage()).getData1() == message.getData1()) {
                        //We have a slurred note with the same value that is waiting for a completion
                        //so we shouldn't add a duplicate.
                        break NoteOnLoop;
                    }
                }
            }
            addEventToTrack(new MidiEvent(message, this.time + offset));
            if (roll) offset += offsetStep;
        }

        offset = 0;

        Collection<MidiEvent> newSlurOffEvents = new LinkedList<>();
        //The off events will occur after the note completion hence the `this.time + tickDuration`
        for (ShortMessage message : sound.getSound().noteOff(channel.midiChannelNum, offVelocity)) {
            long endTime = this.time + tickDuration + offset;

            MidiEvent event = null;
            Iterator<MidiEvent> eventIterator = lastAddedSlurOffs.iterator();
            while (eventIterator.hasNext()) {
                MidiEvent lastOffEvent = eventIterator.next();
                if (((ShortMessage) lastOffEvent.getMessage()).getData1() == message.getData1()) {
                    //The lastOffEvent should be delayed to this off's time
                    lastOffEvent.setTick(endTime);
                    event = lastOffEvent;
                    eventIterator.remove();
                }
            }
            if (event == null) event = new MidiEvent(message, endTime);

            if (slurred) newSlurOffEvents.add(event);
            else         addEventToTrack(event);
            if (roll) offset += offsetStep;
        }

        //Empty the last queue
        for (MidiEvent event : lastAddedSlurOffs) {
            addEventToTrack(event);
        }
        lastAddedSlurOffs.clear();

        //Add all of the newSlurOffEvents
        lastAddedSlurOffs.addAll(newSlurOffEvents);

        //Lastly we need to increase the state time as we just played some music!
        this.time += this.timingEnv.ticksInBeat(sound.getDuration());

        //Keep track of the state time for the end of the song represented by this block
        this.lastTick = this.time + tickDuration + offset;
    }

    //This method repeats all of the events added in the most recent block fragment
    private void addLoopedEvents() {
        //We need to know the duration of a single pass to properly
        //update the state time each loop.
        long passDuration = this.time - this.passStartTime;

        //For each loop add all of events added in the most recent fragment
        for (int i = 0; i < loopCount; i++) {
            for (MidiEvent event : addedThisPass) {
                //The time for the event is going to be the start time relative. To calculate it we will take
                //the relative start time but subtracting the block fragment's original start time (`passStartTime`)
                //from the event's time (`event.getTick()`). Then all that has to be done is shift it up
                //to the most recent time.
                this.track.add(new MidiEvent(event.getMessage(), event.getTick() - this.passStartTime + this.time));
            }
            //Now that we have added an entire loop we can increase the state time accordingly
            this.time += passDuration;
        }

        //Lastly reset the loop data
        this.loopCount = 0;
        this.addedThisPass.clear();
    }

    //This method should be invoked to notify the block that the options have changed. Some changes require
    //additional events to be added to the track or other various state changes.
    protected void updateBlockOptions(Token blockNameToken, BlockOptions options, boolean force) throws InvalidMidiDataException {
        //Save the old options for comparing but use the new options when invoking trackmanager methods
        BlockOptions oldOptions = this.options;
        this.options = options;

        //If a channel is selected and different from the current channel we will make a request for the
        //track manager to change to it.
        if (oldOptions.isChannelSelected() && oldOptions.getSelectedChannel() != this.options.getSelectedChannel()) {
            String reasonForDenial = this.trackManager.requestChannel(this, oldOptions.getSelectedChannel());
            if (reasonForDenial != null) {
                throw new ParseException(blockNameToken, "Cannot select midi channel "+oldOptions.getSelectedChannel()+". Reason: "+reasonForDenial);
            }
        } else {
            this.trackManager.requestChannel(this);
        }

        //Ask the track manager for the channel it is on
        TrackManager.Channel channel = this.trackManager.getChannel(this);

        //If the update is forced or the instrument has changed since the last update then
        //some MIDI program change events need to be added.
        if (force || oldOptions.getInstrument() != this.options.getInstrument()
                || oldOptions.getSoundbank() != this.options.getSoundbank()) {
            //If a soundbank was specified preform the soundbank change sequence described in the
            //[General MIDI Constants](../midi/GeneralMidiConstants.html).
            if (oldOptions.getSoundbank() != 0) {
                this.track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel.midiChannelNum, GeneralMidiConstants.BANK_SELECT_CC_1, GeneralMidiConstants.BANK_SELECT_CC_1_VAL), time));
                this.track.add(new MidiEvent(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel.midiChannelNum, GeneralMidiConstants.BANK_SELECT_CC_2, oldOptions.getSoundbank()), time));
            }
            //Add the program change message
            this.track.add(new MidiEvent(new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel.midiChannelNum, oldOptions.getInstrument(), 0), time));
        }
    }

    //This method is invoked by the compiler when this block in entered.
    protected void enterBlock(Token blockNameToken, BlockOptions options) throws InvalidMidiDataException {
        //Update the options because they may have changed.
        this.updateBlockOptions(blockNameToken, options, false);

        if (options.getLoopCount() > 0) {
            this.loopCount = options.getLoopCount();
        } else {
            this.loopCount = 0;
        }
        //Mark the start time in case we need to loop
        this.passStartTime = this.time;
    }

    //This method is invoked by the compiler when a block is exited.
    protected void leaveBlock() {
        //If a loop request was made now is the time to cash in all the newly
        //added events.
        if (loopCount > 0) addLoopedEvents();
    }

    //When the song is finished we may need to report some "unclosed" operations like a
    //crescendo.
    protected void finish() {
        if (!this.dynBuffer.isEmpty())
            throw new ParseException(this.cresToken, "Gradual change specified but found EOF before the target" +
                    " dynamic is specified.");

        MidiEvent endOfTrackEvent = track.get(track.size()-1);
        track.remove(endOfTrackEvent);
        endOfTrackEvent.setTick(this.lastTick + timingEnv.getPPQ());
        track.add(endOfTrackEvent);
    }
}
