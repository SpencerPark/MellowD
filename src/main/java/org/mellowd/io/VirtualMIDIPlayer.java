package org.mellowd.io;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

//A `VirtualMIDIPlayer` force feeds a sequence to a midi receiver. This simulates play-though
//of the entire sequence without the delay in between notes.
public class VirtualMIDIPlayer {
    //The number of microseconds in each minute (60,000,000). This constant aids
    //in calculating the microseconds per beat for a BPM conversion.
    private static final int MICROSECONDS_PER_MINUTE = 60000000;

    //The number of microseconds in each second (1,000,000).
    private static final double MICROSECONDS_PER_SECOND = 1000000.0;

    //The default tempo (120) describing the number of beats in a minute.
    private static final int DEFAULT_BPM = 120;

    //The default number of micro seconds (&mu;s) per beat. There are `DEFAULT_BPM` beats in a
    //minute and therefor the `DEFAULT_US_PER_BEAT` is <sup>MICROSECONDS_PER_MINUTE</sup>&frasl;<sub>DEFAULT_BPM</sub>
    private static final int DEFAULT_US_PER_BEAT = MICROSECONDS_PER_MINUTE / DEFAULT_BPM;

    //The MIDI meta message identifier for a tempo message.
    private static final byte TEMPO_MIDI_SUBTYPE = 0x51;

    //A utility class for keeping some tracking information about the tracks in the sequence.
    //When iterating over the tacks we need to find the next message in the sequence but the tracks
    //all play concurrently. This class holds the current playback position (as the number of MIDI messages
    //already played) so that we can switch between tracks but remember where we were in the others.
    private class TrackTracker {
        protected Track midiTrack;
        protected int position = 0;

        //Create a new track tracker wrapping the given track.
        public TrackTracker(Track midiTrack) {
            this.midiTrack = midiTrack;
        }

        //Move the pointer forward one position. This is the equivalent of consuming
        //a message.
        public void advancePointer() {
            this.position++;
        }

        //Obtain the next message in the sequence. Note that this does NOT advance the pointer.
        //That should be done manually if it is desired. This method will return null if
        //`hasNext()` returns false.
        public MidiEvent getNext() {
            if (hasNext())
                return midiTrack.get(position);
            return null;
        }

        //Check if there is another message still not consumed in this track.
        public boolean hasNext() {
            return position < midiTrack.size();
        }
    }

    private List<TimeStampedMIDIMessage> stampedMIDIMessages;
    private double duration;

    //Create a new virtual player that is playing the given sequence.
    public VirtualMIDIPlayer(Sequence sequence) {
        if (sequence.getDivisionType() != Sequence.PPQ)
            throw new IllegalArgumentException("Sequence division type was not PPQ. Virtual playback only supports PPQ sequences");

        initStampedMIDIMessages(sequence);
    }

    //Set the sequence playing in this virtual midi player.
    public void setSequence(Sequence sequence) {
        if (sequence.getDivisionType() != Sequence.PPQ)
            throw new IllegalArgumentException("Sequence division type was not PPQ. Virtual playback only supports PPQ sequences");

        initStampedMIDIMessages(sequence);
    }

    private void initStampedMIDIMessages(Sequence sequence) {
        stampedMIDIMessages = new ArrayList<>();

        //Wrap all of the tracks in the sequence in a tracker.
        TrackTracker[] tracks = new TrackTracker[sequence.getTracks().length];
        for (int i = 0; i < tracks.length; i++) {
            tracks[i] = new TrackTracker(sequence.getTracks()[i]);
        }

        //If no tempo is specified this is the tempo
        int usPerBeat = DEFAULT_US_PER_BEAT;
        //The tracks must use the `PPQ` division type and therefor the resolution is in
        //pulses per quarter note or `ticksPerBeat`
        int ticksPerBeat = sequence.getResolution();
        //Track the tick number of the last played message to calculate how much time has elapsed
        long lastTick = 0;
        //The current time is also updated with the lastTick. It tracks the imaginary time (in &mu;s)
        long currentTime = 0;
        //Crawl through the tracks grabbing the first occurring event (tick-wise)
        while (true) {
            MidiEvent nextEvent = null;
            TrackTracker selectedTracker = null;
            for (TrackTracker tracker : tracks) {
                if (tracker.hasNext()) {
                    //Pull the next event on this track
                    MidiEvent event = tracker.getNext();
                    //If we haven't picked an event yet or this event occurs before the selected one
                    //then pick it as the working event
                    if (nextEvent == null || event.getTick() < nextEvent.getTick()) {
                        nextEvent = event;
                        selectedTracker = tracker;
                    }
                }
            }

            //If no tracker is selected that means we have reached the end of the sequence
            //and we can break out of this loop.
            if (selectedTracker == null)
                break;

            //We have pulled a message from this track so update our tracker's pointer
            //to point to the next message
            selectedTracker.advancePointer();

            //Update the current time to properly track the new event
            long tick = nextEvent.getTick();
            //The units for the following calculations look like the following:<br>
            //<sup>&mu;</sup>&fracsl;<sub>beat</sub> &middot; <sup>beat</sup>&fracsl;<sub>endTimeStamp</sub> &middot; endTimeStamp = &mu;
            //This gives us the number of microseconds that has passed since the last tick
            currentTime += ((tick - lastTick) * usPerBeat) / ticksPerBeat;
            //Update the `lastTick` to the current tick
            lastTick = tick;

            MidiMessage msg = nextEvent.getMessage();
            if (msg instanceof MetaMessage) {
                //This is updating synthesiser info and therefore doesn't need to be played back in the
                //receiver. If the message is a tempo change then we need to update our micro seconds per beat.
                if (((MetaMessage) msg).getType() == 0x51) {
                    byte[] data = ((MetaMessage) msg).getData();
                    //The data for this message is the number of micro seconds per beat written over 3 bytes
                    //so we need to combine them again.
                    usPerBeat = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                }
            } else {
                //It should be stamped and remembered
                stampedMIDIMessages.add(new TimeStampedMIDIMessage(currentTime, msg));
            }
        }

        //Convert the duration which is in microseconds, to seconds
        this.duration = currentTime / MICROSECONDS_PER_SECOND;
    }

    //Play the last set sequence fot the given receiver.
    public void playTrackFor(Receiver receiver) {
        for (TimeStampedMIDIMessage message : this.stampedMIDIMessages) {
            message.feedTo(receiver);
        }
    }

    //Get the duration of the last set sequence in seconds.
    public double getDuration() {
        return this.duration;
    }
}
