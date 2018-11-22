package org.mellowd.midi;

import javax.sound.midi.Sequence;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MIDISequence {
    private final TimingEnvironment timingEnvironment;
    private final Map<String, MIDITrack> tracks;

    public MIDISequence(TimingEnvironment timingEnvironment) {
        this.timingEnvironment = timingEnvironment;
        this.tracks = new HashMap<>();
    }

    public Sequence toSequence() {
        Sequence sequence = this.timingEnvironment.createSequence();
        this.tracks.forEach((name, t) -> t.toTrackInSequence(sequence));
        return sequence;
    }

    public MIDITrack getOrCreateTrack(String name) {
        return this.tracks.computeIfAbsent(name, MIDITrack::new);
    }

    public boolean removeTrack(MIDITrack track) {
        return this.tracks.remove(track.getName(), track);
    }

    public Collection<MIDITrack> listTracks() {
        return this.tracks.values();
    }
}
