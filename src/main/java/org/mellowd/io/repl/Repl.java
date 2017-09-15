package org.mellowd.io.repl;

import org.mellowd.compiler.MellowD;
import org.mellowd.io.Compiler;
import org.mellowd.io.*;
import org.mellowd.midi.TimingEnvironment;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.util.Scanner;
import java.util.function.Function;

public class Repl {
    public static void main(String[] args) throws Exception {
        String workingDir = new File(".").getAbsolutePath();
        SourceFinder srcFinder = new CompositeSourceFinder(
                new ResourceSourceFinder(Compiler.FILE_EXTENSION),
                new DirectorySourceFinder(new File(workingDir), Compiler.FILE_EXTENSION)
        );
        TimingEnvironment timingEnvironment = new TimingEnvironment(4, 4, 120);

        MellowD mellowD = new MellowD(srcFinder, timingEnvironment);
        MellowDKernel kernel = new MellowDKernel(mellowD, workingDir);

        SequencePlayer player = null;

        Scanner in = new Scanner(System.in);
        System.out.print("mellowd > ");

        boolean quit = false;
        while (!quit && in.hasNextLine()) {
            System.out.print("mellowd > ");
            String next = in.nextLine();

            switch (next) {
                case ":quit":
                    System.out.println("Stopping...");
                    quit = true;
                    break;
                case ":stop":
                    if (player != null) {
                        System.out.println("Stopping playback");
                        player.stop();
                    } else {
                        System.out.println("Nothing playing");
                    }
                    break;
                default:
                    try {
                        Sequence sequence = kernel.eval(next);

                        if (sequence.getTickLength() > 0) {
                            if (player != null) player.close();
                            player = new SequencePlayer(MidiSystem.getSequencer(), Function.identity(), sequence);
                            player.play(0L);
                        }
                    } catch (Exception e) {
                        System.err.println(e.getLocalizedMessage());
                    }
            }
        }

        kernel.close();
        if (player != null) player.close();
    }
}
