package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.midi.MIDIChannel;

public interface Playable {

    /**
     * Play this playable on the given {@link MIDIChannel}.
     * @param channel the channel this element should be played on.
     */
    void play(MIDIChannel channel);

    /**
     * Calculates the total duration that playing this element will
     * take to play in a given timing environment.
     * @param env the timing environment in which the performance may occur
     * @return the duration of this playable element.
     */
    long calculateDuration(TimingEnvironment env);
}
