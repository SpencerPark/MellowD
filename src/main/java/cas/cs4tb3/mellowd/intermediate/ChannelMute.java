package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.midi.MIDIChannel;
import cas.cs4tb3.mellowd.midi.TimingEnvironment;

public class ChannelMute implements Playable {
    private static final ChannelMute MUTE = new ChannelMute(true);
    private static final ChannelMute UN_MUTE = new ChannelMute(false);

    public static ChannelMute getInstance(boolean mute) {
        if (mute) return MUTE;
        return UN_MUTE;
    }

    private final boolean mute;

    private ChannelMute(boolean mute) {
        this.mute = mute;
    }

    @Override
    public void play(MIDIChannel channel) {
        channel.setMuted(this.mute);
    }

    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
