Mellow D
========

<p id="img_cont">
    <a href="tests/index.html" target="blank">
        <img src="public/images/clipboard-gears-check.png"/>
        Tests
    </a>
    <a href="langRef.html" target="blank">
        <img src="public/images/book.png" />
        Language Specification
    </a>
    <a></a>
    <a></a>
    <a></a>
</p>

What is it?
-----------

Mellow D is a musical notation language. The developer writes a Mellow D source (.mlod) file and 
passes it to the compiler that create a MIDI sequence. This MIDI sequence can then be converted to
various formats with external tools or played directly with various media players such as windows
media player. The compiler also has an option (`-p`) to play the song rather than write the song to a file.

See the language reference above (the book) for more information about the contents of a source file
or checkout the test resources for a few examples.

Documentation
-------------

The documentation was generated from the literate source with a [fork](https://github.com/deezahyn/docco)
of [docco](https://jashkenas.github.io/docco/). With docco installed (the docco command is accessible from
command line) the documented source can be generated with `./gradlew docco` at the top level of the project.

Usage
-----

To build the compiler from the source you can use the gradle wrapper to execute the `build` task. In
the top directory of the project locate the file name `gradlew` or `gradlew.bat`. If on a *nix system
in terminal/console execute `./gradlew build` or use `gradlew build` from the windows command
prompt. You can find the build results inside `build/libs`.

Now that you have a jar place it somewhere it will be comfortable and create a command to launch the
jar. On windows go to a directory that is inside your PATH and execute

```bat
echo java -jar "path\to\the\MellowD-2.0.0.jar" %* > mellowd.bat
```

Similarity on a *nix system you can create an alias with the command
```bash
alias mellowd='java -jar "path/to/the/MellowD-2.0.0.jar"'
```

Now you should be able to execute the `mellowd` command from you terminal. Run `mellowd -h`
for some help with the command line arguments.

Tests
-----

The tests can be run with `./gradlew test` from the top level of the project. The test reports
can then be found in `build/docs/docco/tests/index.html`.

The [Compiler Test](src/test/java/cas/cs4tb3/mellowd/CompilerTest.html) is the main
test runner for the compiler. It takes input files from the `src/test/resources/compilertest`
folder and compiles them putting the result inside `build/resources/test/compilertest/compTestOut`
folder. Due to the compiler output being a sound it is unrealistic to test it automatically. The
output files should be listened to manually for verification. Each of the compiler tests are considered
passing if there are no exception thrown during parsing.

The [Parser Test](src/test/java/cas/cs4tb3/mellowd/ParserTest.html) does note create
any output files. Some details about the parse result are printed to standard out for manual
verification but as there is no intermediate representation a parser tests is considered successful
if the parser accepts the input with no exceptions.

Tools
-----

The parser was generated with antlr [[4]](#ref:4). The [lexer](src/main/antlr/MellowDLexer.html)
and [parser](src/main/antlr/MellowDParser.html) can be found in the `src/main/antlr` source folder. These
grammar files instruct antlr on how to generate a parser that accepts the Mellow D language.

MIDI is the a music protocol that is received by a MIDI synthesizer which can then produce
the sound described in the messages. It is very similar to a networking protocol used in server
communications today. There are a set of accepted messages (or packets in the networking analogy)
that the MIDI synthesizer recognises. As with server protocols, the MIDI protocol has changed overtime
and synthesizers accept various instrument and soundbank mappings. A standard protocol level named GM1 
(General MIDI 1) [[1]](#ref:1) was defined. The Mellow D compiler creates GM1 compatible sound files
to improve the consistency of the playback across various synthesizers (which in most cases in a computer).

A GM1 synthesizer must have 16 channels available with channel 10 designated for percussion. A quote from
the GM1 sound set documentation [[1]](#ref:1) describes how strictly GM1 devices must bind to the sound set:
> <div>
General MIDI's most recognized feature is the defined list of sounds (or "patches"). However, General MIDI does not actually define the way the sound will be reproduced, only the name of that sound.&nbsp;Though this can obviously result in wide variations in performance from the same song data on different GM sound sources, the authors of General MIDI felt it important to allow each manufacturer to have their own ideas and express their personal aesthetics when it comes to picking the exact timbres for each sound.<br /><br />Each manufacturer must insure that their sounds provide an acceptable representation of song data written for General MIDI. Guidelines for developing GM compatible sound sets and song data are available.<br />
<ul>
<li>The names of the instruments indicate what sort of sound will be heard when that instrument number (MIDI Program Change or "PC#") is selected on the GM1 synthesizer.<br /><br /></li>
<li>These sounds are the same for all MIDI Channels except Channel 10, which has only "percussion" sounds.</li>
</ul>
</div>

In Mellow D these numbers are all mapped from nice names. The percussion names can be found in
[GeneralMidiPercussion](src\main\java\cas\cs4tb3\mellowd\midi\GeneralMidiPercussion.html) and the instrument
names in [GeneralMidiInstrument](src\main\java\cas\cs4tb3\mellowd\midi\GeneralMidiInstrument.html).

References
----------

The following online resources were frequently consulted during the development of
Mellow D:

<a name="ref:1"></a>[1] "GM 1 Sound Set", Midi.org, 2016. [Online]. 
Available: https://www.midi.org/specifications/item/gm-level-1-sound-set. [Accessed: 01- Apr- 2016].

<a name="ref:2"></a>[2]"Summary of MIDI Messages", Midi.org, 2016. [Online]. 
Available: https://www.midi.org/specifications/item/table-1-summary-of-midi-message. [Accessed: 01- Apr- 2016].

<a name="ref:3"></a>[3]"Control Change Messages (Data Bytes)", Midi.org, 2016. [Online].
Available: https://www.midi.org/specifications/item/table-3-control-change-messages-data-bytes-2. [Accessed: 01- Apr- 2016].

<a name="ref:4"></a>[4]T.  Parr, "ANTLR 4 Documentation", GitHub, 2015. [Online]. 
Available: https://github.com/antlr/antlr4/blob/master/doc/index.md. [Accessed: 01- Apr- 2016].
