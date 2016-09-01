package cas.cs4tb3.mellowd.compiler;

import com.sun.media.sound.AudioSynthesizer;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class WavIODelegate implements SequenceIODelegate {
    private static final WavIODelegate instance = new WavIODelegate();

    public static WavIODelegate getInstance() {
        return instance;
    }

    private WavIODelegate() { }

    private static final float SAMPLE_RATE = 96000; //Hz
    private static final int SAMPLE_SIZE = 24; //bits per sample
    private static final int CHANNELS_MONO = 1;
    private static final int CHANNELS_STEREO = 2;
    private static final Map<String, Object> AUDIOSTREAM_PROPERTIES = new HashMap<>();
    static {
        AUDIOSTREAM_PROPERTIES.put("interpolation", "sinc");
        AUDIOSTREAM_PROPERTIES.put("max polyphony", "1024");
    }

    @Override
    public void save(Sequence sequence, OutputStream out) throws IOException {
        VirtualMIDIPlayer player = new VirtualMIDIPlayer(sequence);

        AudioSynthesizer synth;
        try {
            synth = (AudioSynthesizer) MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException | ClassCastException e) {
            throw new IOException("Cannot open audio synth for virtual playback.", e);
        }
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS_STEREO, true, false);
        AudioInputStream stream;
        try {
            stream = synth.openStream(format, AUDIOSTREAM_PROPERTIES);
        } catch (MidiUnavailableException e) {
            throw new IOException("Cannot open audio synth for virtual playback. Reason: " + e.getLocalizedMessage(), e);
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
