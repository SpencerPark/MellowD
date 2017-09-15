package org.mellowd.io;

import java.io.PrintStream;

public class ArgParser {
    public static class Help extends Exception {
        public Help(String message) {
            super(message);
        }
    }

    private static final String USAGE_MESSAGE = "" +
            "usage: mellowd [-h] [-p] [-ts numerator denominator] [-t tempo]\n" +
            "               [-o output_dir] [-s source_dir]... [-sf font]...\n" +
            "               [-pl plugin] [-wav] [-mid] [--silent]           \n" +
            "               [source_file]";

    private static final String OPT_DESC_HELP = "" +
            "    -h: display help about the usage of the mellowd command. Any\n" +
            "        other flags given will act as a help filter and limit   \n" +
            "        what is displayed                                        ";
    private static final String OPT_DESC_PLAY = "" +
            "    -p, --play: play the compilation result through the sound   \n" +
            "                system controlled by the default MIDI device     ";
    private static final String OPT_DESC_TIMESIG = "" +
            "    -ts, --timesig: set the time signature for the compilation  \n" +
            "      numerator: the number of beats in a bar (default 4)       \n" +
            "      denominator: the type of note that gets 1 beat (default 4) ";
    private static final String OPT_DESC_TEMPO = "" +
            "    -t, --tempo: set the tempo for the compilation              \n" +
            "      tempo: an integer tempo in beats per minute (default 120)  ";
    private static final String OPT_DESC_OUTDIR = "" +
            "    -o, -outdir: set the directory in which the compilation     \n" +
            "                 should be saved in                             \n" +
            "      output_dir: path to the output directory. Absolute path or\n" +
            "                  relative to the calling directory (default ./) ";
    private static final String OPT_DESC_SRCDIR = "" +
            "    -s, --src: add a directory to the list of paths to check for\n" +
            "               other source files including imports. This option\n" +
            "               may be given multiple times to add multiple roots\n" +
            "      source_dir: path to the source directory. Absolute path or\n" +
            "                  relative to the calling directory.             ";
    private static final String OPT_DESC_SOUNDFONT = "" +
            "    -sf, --soundfont: add a soundfont file to the list of font  \n" +
            "                      paths to be loaded during MIDI playback.  \n" +
            "      font: path to the sound font (conventionally *.sf2, *.dls)\n" +
            "            Absolute path or relative to the calling directory.  ";
    private static final String OPT_DESC_PLUGIN = "" +
            "    -pl, --plugin: add a plugin id to the list of plugins to    \n" +
            "                   apply during execution.                      \n" +
            "      plugin: the plugin id to apply";
    private static final String OPT_DESC_WAV = "" +
            "    -wav, --wave: set the output to include a .wav file. If no  \n" +
            "                  output modifier is given (-p, -wav, -mid) the \n" +
            "                  output will be MIDI                            ";
    private static final String OPT_DESC_MID = "" +
            "    -mid, --midi: set the output to include a .mid file. If no  \n" +
            "                  output modifier is given (-p, -wav, -mid) the \n" +
            "                  output will be MIDI                           ";
    private static final String OPT_DESC_SILENT = "" +
            "    --silent: prevent the compiler from producing any messages  ";
    private static final String OPT_DESC_SOURCE = "" +
            "    source_file: set the path to the file to compile. It can be \n" +
            "                 an absolute path or a path relative to the     \n" +
            "                 calling directory.                             \n" +
            "                 If omitted, standard in will be used as input   ";

    /**
     * Calls {@link #parse(String[])} and prints and {@link Help} to
     * standard out. In this case it returns {@code null}
     * @param args the command line arguments to parse as {@link CompilerOptions}
     * @return the {@link CompilerOptions} described by the {@code args} or {@code null}
     *         if {@link Help} was thrown.
     */
    public static CompilerOptions parseOrPrint(String[] args) {
        try {
            return parse(args);
        } catch (Help help) {
            System.out.println(help.getMessage());
            return null;
        }
    }

    /**
     * Calls {@link #parse(String[])} and prints and {@link Help} to
     * {@code out}. In this case it returns {@code null}
     * @param args the command line arguments to parse as {@link CompilerOptions}
     * @param out the stream to print to
     * @return the {@link CompilerOptions} described by the {@code args} or {@code null}
     *         if {@link Help} was thrown.
     */
    public static CompilerOptions parseOrPrint(String[] args, PrintStream out) {
        try {
            return parse(args);
        } catch (Help help) {
            out.println(help.getMessage());
            return null;
        }
    }

    /**
     * Parses the given {@code args} as {@link CompilerOptions}. If the help
     * flag (-h) is given this method throws a {@link Help} message describing
     * the usage. If an error occurs, again a {@link Help} message is thrown. <br/>
     * See: {@link #parseOrPrint(String[])} for an implementation that simply prints
     * the help rather than throwing it.
     * @param args the command line arguments to parse as {@link CompilerOptions}
     * @return the {@link CompilerOptions} described by the {@code args}
     * @throws Help if the help flag (-h) is given or an error is encountered while
     *              parsing.
     */
    public static CompilerOptions parse(String[] args) throws Help {
        CompilerOptions.Builder options = new CompilerOptions.Builder();

        StringBuilder help = new StringBuilder(USAGE_MESSAGE);
        help.append('\n');

        boolean showHelp = false;
        boolean parsedSourceFileArg = false;
        int i = 0;
        while (i < args.length) {
            switch (args[i].toLowerCase()) {
                case "-h":
                case "--help":
                    showHelp = true;
                    i++;
                    break;
                case "-ts":
                case "--timesig":
                    help.append('\n').append(OPT_DESC_TIMESIG);
                    if (!showHelp) i = parseTimeSignature(options, args, i + 1);
                    else i++;
                    break;
                case "-t":
                case "--tempo":
                    help.append('\n').append(OPT_DESC_TEMPO);
                    if (!showHelp) i = parseTempo(options, args, i + 1);
                    else i++;
                    break;
                case "-o":
                case "--outdir":
                    help.append('\n').append(OPT_DESC_OUTDIR);
                    if (!showHelp) i = parseOutdir(options, args, i + 1);
                    else i++;
                    break;
                case "-s":
                case "--src":
                    help.append('\n').append(OPT_DESC_SRCDIR);
                    if (!showHelp) i = parseSourceDir(options, args, i + 1);
                    else i++;
                    break;
                case "-sf":
                case "--soundfont":
                    help.append('\n').append(OPT_DESC_SOUNDFONT);
                    if (!showHelp) i = parseSoundFont(options, args, i + 1);
                    else i++;
                    break;
                case "-pl":
                case "--plugin":
                    help.append('\n').append(OPT_DESC_PLUGIN);
                    if (!showHelp) i = parsePlugin(options, args, i + 1);
                    else i++;
                    break;
                case "-p":
                case "--play":
                    help.append('\n').append(OPT_DESC_PLAY);
                    i++;
                    options.enableOutputType(CompilerOptions.OUTPUT_LIVE_AUDIO);
                    break;
                case "-wav":
                case "--wave":
                    help.append('\n').append(OPT_DESC_WAV);
                    i++;
                    options.enableOutputType(CompilerOptions.OUTPUT_WAV);
                    break;
                case "-mid":
                case "--midi":
                    help.append('\n').append(OPT_DESC_MID);
                    i++;
                    options.enableOutputType(CompilerOptions.OUTPUT_MIDI);
                    break;
                case "--silent":
                    help.append('\n').append(OPT_DESC_SILENT);
                    i++;
                    options.setSilent(true);
                    break;
                default:
                    if (parsedSourceFileArg)
                        throw new IllegalArgumentException("Already parsed source file. The following was a surprise to me: " + args[i]);
                    parsedSourceFileArg = true;
                    help.append('\n').append(OPT_DESC_SOURCE);
                    options.setSource(args[i]);
                    i++;
                    break;
            }
        }

        if (showHelp) {
            if (args.length == 1) {
                //The only flag is -h
                help.append('\n').append(OPT_DESC_HELP);
                help.append('\n').append(OPT_DESC_TIMESIG);
                help.append('\n').append(OPT_DESC_TEMPO);
                help.append('\n').append(OPT_DESC_OUTDIR);
                help.append('\n').append(OPT_DESC_SRCDIR);
                help.append('\n').append(OPT_DESC_SOUNDFONT);
                help.append('\n').append(OPT_DESC_PLUGIN);
                help.append('\n').append(OPT_DESC_PLAY);
                help.append('\n').append(OPT_DESC_WAV);
                help.append('\n').append(OPT_DESC_MID);
                help.append('\n').append(OPT_DESC_SILENT);
                help.append('\n').append(OPT_DESC_SOURCE);
            }
            throw new Help(help.toString());
        }

        return options.build();
    }

    private static int checkPositive(String name, int value) throws Help {
        if (value < 0)
            throw new Help(String.format("[Parse Error]: %s expected a positive number but '%d' was given", name, value));
        return value;
    }

    private static int parseInt(String name, String value) throws Help {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new Help(String.format("[Parse Error]: %s expected a number but was given '%s'", name, value));
        }
    }

    private static int parseTimeSignature(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 2)
            throw new Help("[Parse Error]: Expected time signature numerator and denominator to follow " + args[pos - 1]);

        int numerator = checkPositive("time signature numerator", parseInt("time signature numerator", args[pos]));
        int denominator = checkPositive("time signature denominator", parseInt("time signature denominator", args[pos + 1]));

        options.setTimeSignature(numerator, denominator);

        return pos + 2;
    }

    private static int parseTempo(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 1)
            throw new Help("[Parse Error]: Expected tempo number in bpm to follow " + args[pos - 1]);

        int tempo = checkPositive("tempo", parseInt("tempo", args[pos]));

        options.setTempo(tempo);

        return pos + 1;
    }

    private static int parseOutdir(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 1)
            throw new Help("[Parse Error]: Expected path to output directory to follow " + args[pos - 1]);

        String outDir = args[pos];

        options.setOutputDir(outDir);

        return pos + 1;
    }

    private static int parseSourceDir(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 1)
            throw new Help("[Parse Error]: Expected path to the source directory to follow " + args[pos - 1]);

        String sourceDir = args[pos];

        options.addSourceDirectory(sourceDir);

        return pos + 1;
    }

    private static int parseSoundFont(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 1)
            throw new Help("[Parse Error]: Expected path to the sound font to follow " + args[pos - 1]);

        String soundFont = args[pos];

        options.addSoundFont(soundFont);

        return pos + 1;
    }

    private static int parsePlugin(CompilerOptions.Builder options, String[] args, int pos) throws Help {
        if (args.length < pos + 1)
            throw new Help("[Parse Error]: Expected plugin id to follow " + args[pos - 1]);

        String pluginId = args[pos];

        options.addPlugin(pluginId);

        return pos + 1;
    }
}
