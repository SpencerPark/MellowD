package org.mellowd.io.live;

import org.mellowd.compiler.MellowD;
import org.mellowd.io.Compiler;
import org.mellowd.io.*;
import org.mellowd.midi.TimingEnvironment;

import java.io.File;
import java.util.Scanner;

public class Repl {
    public static void main(String[] args) throws Exception {
        String workingDir = new File(".").getAbsolutePath();
        SourceFinder srcFinder = new CompositeSourceFinder(
                new ResourceSourceFinder(Compiler.FILE_EXTENSION),
                new DirectorySourceFinder(new File(workingDir), Compiler.FILE_EXTENSION)
        );
        TimingEnvironment timingEnvironment = new TimingEnvironment(4, 4, 120);

        MellowD mellowD = new MellowD(srcFinder, timingEnvironment);
        MellowDSession session = new MellowDSession(mellowD, workingDir);
//        session.eval("def block Piano { instrument: \"piano\" } \n");
//        session.eval("def block Guitar { instrument: \"acoustic guitar\" }\n");
//        session.eval("Piano { [b]*<e, e, e, e, e, e, e, e> }");
//        session.eval("Guitar { [C~, *, *, *]*<q> }");

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
                default:
                    try {
                        session.eval(next);
                    } catch (Exception e) {
                        System.err.println(e.getLocalizedMessage());
                    }
            }
        }
    }
}
