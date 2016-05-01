package cas.cs4tb3.mellowd.parser;

import cas.cs4tb3.mellowd.Dynamic;
import cas.cs4tb3.mellowd.TimingEnvironment;
import org.antlr.v4.runtime.Token;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Track;
import java.util.*;

//This is a state based class that is tracking the virtual state of the playback sequencer
//that help make decisions on which channel a channel specific event should happen on.
public class TrackManager {
    public static class Channel {
        public final boolean isDrum;
        public final int midiChannelNum;
        public final Set<Block> usedBy = new HashSet<>();

        private Channel(boolean isDrum, int midiChannelNum) {
            this.isDrum = isDrum;
            this.midiChannelNum = midiChannelNum;
        }
    }

    private Map<String, Block> blocks;
    private Map<String, Channel> channelMap;

    private List<Channel> channels;
    private List<Channel> drumChannels;

    public TrackManager(Set<Integer> regChannels, Set<Integer> drumChannels) {
        //Check a few preconditions to make sure we have the expected channel inputs.
        if (regChannels == null || regChannels.isEmpty())
            throw new IllegalArgumentException("No regular channels specified.");
        if (drumChannels == null || drumChannels.isEmpty())
            throw new IllegalArgumentException("No drum channels specified.");

        blocks = new HashMap<>();
        this.channels = new ArrayList<>(regChannels.size());
        this.drumChannels = new ArrayList<>(drumChannels.size());
        for (int channel : regChannels) {
            this.channels.add(new Channel(false, channel));
        }
        for (int dChannel : drumChannels) {
            this.drumChannels.add(new Channel(true, dChannel));
        }
        this.channelMap = new HashMap<>();
        this.blocks = new HashMap<>();
    }

    public Block createBlock(Token blockToken, TimingEnvironment timingEnvironment, Track track, BlockOptions options) {
        //Check to make sure the block doesn't exist yet
        if (this.blocks.containsKey(blockToken.getText()))
            throw new ParseException(blockToken, "A block with the name " + blockToken.getText() + " already exists.");

        //Instantiate the new block
        Block block = new Block(this, timingEnvironment, track, options, blockToken.getText(), Dynamic.mf);

        //Register the block
        this.blocks.put(block.getName(), block);

        //Put it on a channel
        requestChannel(block);

        try {
            block.updateBlockOptions(blockToken, options, true);
        } catch (InvalidMidiDataException e) {
            throw new ParseException(blockToken, "Invalid midi data in " + blockToken.getText() + "'s block options. "+e.getLocalizedMessage());
        }

        //Return the newly created block
        return block;
    }

    public Block getBlock(String blockName) {
        return this.blocks.get(blockName);
    }

    public void finish() {
        for (Block block : this.blocks.values()) {
            block.finish();
        }
    }

    public void requestChannel(Block block) {
        List<Channel> empties = new LinkedList<>();
        if (block.getOptions().isPercussion()) {
            for (Channel channel : drumChannels) {
                if (channel.usedBy.isEmpty()) {
                    //Don't take an empty channel yet because it would be better to share if possible.
                    empties.add(channel);
                } else {
                    for (Block user : channel.usedBy) {
                        if (block.getName().equals(user.getOptions().getShareChannel())
                                || user.getName().equals(block.getOptions().getShareChannel())) {
                            //Found a channel to share! We can pack these together.
                            switchChannels(channel, block);
                            return;
                        }
                    }
                }
            }
            if (empties.isEmpty()) {
                //Because this block is a percussion block it is not that bad if
                //we have to share a channel.
                switchChannels(drumChannels.get(0), block);
            } else {
                //Take the first empty channel.
                switchChannels(empties.get(0), block);
            }
        } else {
            Channel current = channelMap.get(block.getName());
            if (current != null && !current.isDrum) return;

            for (Channel channel : channels) {
                if (channel.usedBy.isEmpty()) {
                    //Don't take an empty channel yet because it would be better to share if possible.
                    empties.add(channel);
                } else {
                    for (Block user : channel.usedBy) {
                        if (block.getName().equals(user.getOptions().getShareChannel())
                                || user.getName().equals(block.getOptions().getShareChannel())) {
                            //Found a channel to share! We can pack these together.
                            switchChannels(channel, block);
                            return;
                        }
                    }
                }
            }
            if (empties.isEmpty()) {
                //Otherwise we really can't just put 2 random regular blocks together because at the very
                //least we may have an instrument mismatch. This is a problem we unfortunately cannot
                //fix.
                throw new IllegalStateException("Ran out of channels. Mellow D source is overloaded and cannot fit in the MIDI channels." +
                        " Try to use the \"sharechannel\" options to compact blocks on the same channel.");
            } else {
                //Take the first empty channel.
                switchChannels(empties.get(0), block);
            }
        }
    }

    public String requestChannel(Block block, int channelNum) {
        if (block.getOptions().isPercussion()) {
            for (Channel channel : drumChannels) {
                if (channel.midiChannelNum == channelNum) {
                    //Try and take this channel
                    if (channel.usedBy.isEmpty()) {
                        switchChannels(channel, block);
                        return null;
                    } else {
                        for (Block user : channel.usedBy) {
                            if (block.getName().equals(user.getOptions().getShareChannel())
                                    || user.getName().equals(block.getOptions().getShareChannel())) {
                                //The channel is occupied but a sharing relationship has been specified.
                                switchChannels(channel, block);
                                return null;
                            }
                        }
                        return "The requested channel (" + channelNum + ") is occupied by a block that does not allow sharing.";
                    }
                }
            }
            return "The requested channel (" + channelNum + ") is not an available drum channel and the block (" + block.getName() + ") is a percussion block.";

        } else {
            for (Channel channel : channels) {
                if (channel.midiChannelNum == channelNum) {
                    //Try and take this channel
                    if (channel.usedBy.isEmpty()) {
                        switchChannels(channel, block);
                        return null;
                    } else {
                        for (Block user : channel.usedBy) {
                            if (block.getName().equals(user.getOptions().getShareChannel())
                                    || user.getName().equals(block.getOptions().getShareChannel())) {
                                //The channel is occupied but a sharing relationship has been specified.
                                switchChannels(channel, block);
                                return null;
                            }
                        }
                        return "The requested channel (" + channelNum + ") is occupied by a block that does not allow sharing.";
                    }
                }
            }
            return "The requested channel (" + channelNum + ") is not an available channel and the block (" + block.getName() + ") is a regular block.";
        }
    }

    private void joinChannel(Channel channel, Block block) {
        channel.usedBy.add(block);
        this.channelMap.put(block.getName(), channel);
    }

    private void leaveChannel(Channel channel, Block block) {
        channel.usedBy.remove(block);
        this.channelMap.remove(block.getName());
    }

    private void switchChannels(Channel to, Block block) {
        Channel from = channelMap.get(block.getName());
        if (from != null) leaveChannel(from, block);
        joinChannel(to, block);
    }

    public Channel getChannel(Block block) {
        Channel channel = this.channelMap.get(block.getName());
        if (channel != null)
            return channel;
        this.requestChannel(block);
        return this.channelMap.get(block.getName());
    }
}
