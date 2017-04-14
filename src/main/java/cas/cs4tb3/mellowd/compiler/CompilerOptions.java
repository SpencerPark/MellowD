package cas.cs4tb3.mellowd.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilerOptions {
    public static final int OUTPUT_MIDI = 0x1;
    public static final int OUTPUT_WAV = 0x2;
    public static final int OUTPUT_LIVE_AUDIO = 0x4;

    public static class Builder {
        private String outputDir;
        private int timeSignature = 0x0404;
        private int tempo = 0;
        private int outputType = 0;
        private List<String> sourceDirs = new ArrayList<>();
        private List<String> soundFonts = new ArrayList<>();
        private String source = "";
        private boolean silent = false;

        public Builder() {
        }

        public Builder setOutputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder setTimeSignature(int numerator, int denominator) {
            this.timeSignature = ((numerator & 0xFF) << 8) | (denominator & 0xFF);
            return this;
        }

        public Builder setTempo(int tempo) {
            this.tempo = tempo;
            return this;
        }

        public Builder enableOutputType(int outputTypeFlag) {
            this.outputType |= outputTypeFlag;
            return this;
        }

        public Builder addSourceDirectory(String sourceDir) {
            this.sourceDirs.add(sourceDir);
            return this;
        }

        public Builder addSoundFont(String soundFontPath) {
            this.soundFonts.add(soundFontPath);
            return this;
        }

        public Builder setSilent(boolean silent) {
            this.silent = silent;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public CompilerOptions build() {
            return new CompilerOptions(
                    this.outputDir == null ? "" : this.outputDir,
                    this.timeSignature == 0 ? 0x0404 : this.timeSignature,
                    this.tempo == 0 ? 120 : this.tempo,
                    this.outputType == 0 ? OUTPUT_MIDI : this.outputType,
                    this.sourceDirs == null ? Collections.emptyList() : this.sourceDirs,
                    this.soundFonts == null ? Collections.emptyList() : this.soundFonts,
                    this.silent,
                    this.source == null ? "" : this.source
            );
        }
    }

    private final String outputDir;
    private final int timeSignature;
    private final int tempo;
    private final int outputType;
    private final List<String> sourceDirs;
    private final List<String> soundFonts;
    private final boolean silent;
    private final String source;

    public CompilerOptions(String outputDir, int timeSignature, int tempo, int outputType, List<String> sourceDirs, List<String> soundFonts, boolean silent, String source) {
        this.outputDir = outputDir;
        this.timeSignature = timeSignature;
        this.tempo = tempo;
        this.outputType = outputType;
        this.sourceDirs = sourceDirs;
        this.soundFonts = soundFonts;
        this.silent = silent;
        this.source = source;
    }

    /**
     * Get the directory that compiled files should be placed in.
     *
     * @return the output directory
     */
    public String getOutputDirectory() {
        return this.outputDir;
    }

    /**
     * Get the numerator of the time signature. This number
     * describes the number of beats in a bar.
     *
     * @return the numerator of the time signature
     */
    public int getTimeSignatureTop() {
        return (this.timeSignature & 0xFF00) >>> 8;
    }

    /**
     * Get the denominator of the time signature. This number
     * describes the type of note that is one beat. For example
     * a quarter note is one beat in common time and therefor the
     * denominator is 4.
     *
     * @return the denominator of the time signature
     */
    public int getTimeSignatureBottom() {
        return (this.timeSignature & 0xFF);
    }

    /**
     * Get the tempo in beats per minute
     *
     * @return the tempo
     */
    public int getTempo() {
        return this.tempo;
    }

    /**
     * Check if the options specify that a MIDI file should be part
     * of the outputs.
     *
     * @return true if the compiler should output a MIDI file, false otherwise
     */
    public boolean shouldOutputMIDI() {
        return shouldOutput(OUTPUT_MIDI);
    }

    /**
     * Check if the options specify that a WAV file should be part
     * of the outputs.
     *
     * @return true if the compiler should output a WAV file, false otherwise
     */
    public boolean shouldOutputWAV() {
        return shouldOutput(OUTPUT_WAV);
    }

    /**
     * Check if the compiler should play the compiled audio directly.
     *
     * @return true if the compiler should play the output
     */
    public boolean shouldPlayLive() {
        return shouldOutput(OUTPUT_LIVE_AUDIO);
    }

    /**
     * Check if a certain output is requested. <br/>
     * The valid flags are: {{@link #OUTPUT_MIDI}, {@link #OUTPUT_WAV}, {@link #OUTPUT_LIVE_AUDIO}}
     *
     * @param outputTypeFlag the output flag to test for
     *
     * @return true if the output described by the {@code outputTypeFlag} should
     * be included in the outputs by the compiler
     */
    public boolean shouldOutput(int outputTypeFlag) {
        return (this.outputType & outputTypeFlag) != 0;
    }

    /**
     * Get the paths of the directories that can be checked for source
     * code.
     *
     * @return a list of source directory paths
     */
    public List<String> getSourceDirs() {
        return this.sourceDirs;
    }

    /**
     * Get the paths of the soundfonts to be loaded during playback or virtual
     * playback/recording. The convention for these files is to end with the
     * {@code .sf2} or {@code .dls} file extension.
     *
     * @return a list of soundfont paths
     */
    public List<String> getSoundFonts() {
        return this.soundFonts;
    }

    /**
     * Check if the options specify that the compiler should be silent
     * and not print out the extra messages.
     *
     * @return true if the compiler should skip the verbose messages, false otherwise
     */
    public boolean wantsSilent() {
        return this.silent;
    }

    /**
     * Check if the options specify that the compiler should be loud
     * and print out the extra messages.
     *
     * @return true if the compiler should include the verbose messages, false otherwise
     */
    public boolean wantsVerbose() {
        return !this.silent;
    }

    /**
     * Get the path to the root source file to compile
     *
     * @return the path to the source file to compile
     */
    public String getSource() {
        return this.source;
    }
}
