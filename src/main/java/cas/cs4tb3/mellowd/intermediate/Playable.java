package cas.cs4tb3.mellowd.intermediate;

/**
 * Created on 2016-06-14.
 */
public interface Playable {

    /**
     * Play this playable on the given {@link MIDIChannel}.
     * @param channel the channel this element should be played on.
     */
    void play(MIDIChannel channel);
}
