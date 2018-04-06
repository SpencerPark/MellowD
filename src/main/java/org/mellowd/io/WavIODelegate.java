package org.mellowd.io;

import com.sun.media.sound.AudioSynthesizer;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WavIODelegate implements SequenceIODelegate {
    private static final float SAMPLE_RATE = 44100; //Hz
    private static final int SAMPLE_SIZE = 24; //bits per sample
    private static final int CHANNELS_MONO = 1;
    private static final int CHANNELS_STEREO = 2;
    private static final Map<String, Object> AUDIOSTREAM_PROPERTIES = new HashMap<>();
    static {
        AUDIOSTREAM_PROPERTIES.put("interpolation", "sinc");
        AUDIOSTREAM_PROPERTIES.put("max polyphony", "1024");
    }

    private final Function<Synthesizer, Synthesizer> soundfontLoader;

    private int channels = CHANNELS_STEREO;
    private float sampleRate = SAMPLE_RATE;
    private int sampleSize = SAMPLE_SIZE;

    public WavIODelegate(Function<Synthesizer, Synthesizer> soundfontLoader) {
        this.soundfontLoader = soundfontLoader;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    @Override
    public void save(Sequence sequence, OutputStream out) throws IOException {
        VirtualMIDIPlayer player = new VirtualMIDIPlayer(sequence);

        AudioFormat format = new AudioFormat(this.sampleRate, this.sampleSize, this.channels, true, false);
        AudioSynthesizer synth;
        AudioInputStream stream;
        try {
            synth = (AudioSynthesizer) MidiSystem.getSynthesizer();
            stream = synth.openStream(format, AUDIOSTREAM_PROPERTIES);
            if (this.soundfontLoader != null)
                synth = (AudioSynthesizer) this.soundfontLoader.apply(synth);
        } catch (MidiUnavailableException e) {
            throw new IOException("Cannot open audio synth for virtual playback. Reason: " + e.getLocalizedMessage(), e);
        } catch (ClassCastException e) {
            throw new IOException("System synth is not an instance of AudioSynthesizer. '.wav' conversion not supported");
        }

        // Play Sequence into AudioSynthesizer Receiver.
        try {
            player.playTrackFor(synth.getReceiver());
        } catch (MidiUnavailableException e) {
            throw new IOException("Cannot play the track back to the receiver. Reason: " + e.getLocalizedMessage(), e);
        }

        // Calculate how long the WAV file needs to be
        final long len = (long) (stream.getFormat().getFrameRate() * player.getDuration());
        stream = new AudioInputStream(stream, stream.getFormat(), len);

        // Write WAVE file to disk.
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, out);

        synth.close();
    }

    @Override
    public String getExtension() {
        return ".wav";
    }
}
