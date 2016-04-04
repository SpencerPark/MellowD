Mellow D Language Reference
===========================

Overview
--------

<ol>
<li>[Base Elements](#base-elements)</li>
    <ol>
    <li>[Notes](#notes)</li>
    <li>[Articulation](#articulation)</li>
    <li>[Beat](#beat)</li>
    <li>[Dynamics](#dynamics)</li>
    <li>[Percussion](#percussion)</li>
    </ol>
<li>[Complex Elements](#complex-elements)</li>
    <ol>
    <li>[Chords](#chords)</li>
    <li>[Melodies](#melodies)</li>
    <li>[Rhythms](#rhythms)</li>
    <li>[Phrases](#phrases)</li>
    </ol>
<li>[Top Level Structures](#top-level-structures)</li>
    <ol>
    <li>[Variables](#variables)</li>
    <li>[Blocks](#blocks)</li>
    <li>[Block Options](#block-options)</li>
    </ol>
</ol>

Base Elements
=============

Notes
-----

A note is the base element in Mellow D. It is the building block of sounds. Notes
are a note char (lowercase `a` to `g`) followed by a flat (`$`) or sharp (`#`) token
followed by an octave shift (`+` or `-` followed by a number `0` to `9`). The `*` character
defines a rest.

Ex:

| Musical Description | Mellow D Source|
|---------------------|----------------|
| a flat              | `a$`           |
| b sharp             | `b#`           |
| c sharp up 2 octaves| `c#+2`         |
| rest                | `*`            |

Articulation
------------

Articulation affects how a note or chord is performed. Articulation can
be applied to a [chord](#chords) or a [note](#notes) as long as the note
is appearing in a [melody](#melodies). For example `a$.` would preform an
*a flat* with an abrupt stop and a shorter duration (staccato). `(c, e, g)~`
would perform a *C* chord playing each note quickly in succession creating
a *roll*. See the [Articulation](src/main/java/cas/cs4tb3/mellowd/Articulation.html) 
class for more detail on each articulation.

Mellow D supports the following articulation mappings:

| Articulation | Mellow D Source|
|--------------|----------------|
| STACCATO     | `.`            |
| STACCATISSIMO| `!`            |
| MARCATO      | `^`            |
| ACCENT       | `` ` ``        |
| TENUTO       | `_`            |
| GLISCANDO    | `~`            |

Beat
----

Beats describe the duration of a sound. Mellow D supports `w`, `h`, `q`, `e` and `s`
to represent the musical *whole*, *half*, *quarter*, *eight* and *sixteenth* note
respectively. A beats duration can be extended just like a musical __&middot;__ using 
0 or more `.` following the beat token. Each additional `.` adds half of the previous
added value. See the [Beat](src/main/java/cas/cs4tb3/mellowd/Beat.html) class for more
detail on how the dot functions. Beats are the building blocks of [Rhythms](#rhythms).

Dynamics
--------

Dynamics affect the volume of the sound preformed. Mellow D supports the following dynamic
tokens in increasing order of volume: `pppp`, `ppp`, `pp`, `p`, `mp`, `mf`, `f`, `ff`, `fff`, `ffff`.
By default the dynamic is `mf`. Dynamic tokens appear anywhere a [phrase](#phrases) can appear
and they set the dynamic for phrases following it in the [block](#blocks) it appears in. The dynamic
can be set as often as desired and functions the same way it does if written in a piece of sheet music.

Crescendo (`<<`) and decrescendo (`>>`) tokens can appear directly following a dynamic token resulting
in the volume gradually changing until it reaches the next dynamic. For example `ff >>` *phrases* `mf`
would gradually drop the volume from double forte to mezzoforte over the *phrases*.

Percussion
----------

Percussion can be considered a mode in Mellow D. In percussion mode there are an additional set of
identifiers for describing percussion sounds. They appear anywhere a note normally would. The full list
of supported sounds can be found in [GeneralMidiPercussion](src/main/java/cas/cs4tb3/mellowd/midi/GeneralMidiPercussion.html).
On a percussion channel playing a MIDI note plays its percussion mapping's sound. This means that the
percussion sounds can be represented as pitches and can actually be preformed as percussion or as their
respective pitch. It is the developers responsibility to make sure that percussion sounds are played in
percussion blocks as the compiler directly resolves them to a pitch and has no way of realizing if it
was defined as percussion or not. See [block options](#block-options) for how to put a block in percussion mode
and the [variables](#variables) section for how to define a variable in percussion mode.

Complex Elements
================

Mellow D sounds are created from 3 complex elements components. [Chords](#chords), [Melodies](#melodies)
and [Rhythms](#rhythms). All 3 of these types can be defined as a [variable](#variables) and
variable values can be concatenated with new element definitions by using the variable identifier
in the definition. For example if `myChord` is defined as a [Chord](#chords), a new chord
containing all the notes in `myChord` can be declared as `(myChord, c, e, g)`. A [phrase](#phrases) is
the product of playing a [melody](#melodies) or [chord](#chords) over a [rhythm](#rhythms).

Chords
------

A chord is a collection of [notes](#notes) that are performed concurrently. In
the Mellow D source a chord is defined as an opening `(` followed by a [note](#notes)
or multiple [notes](#notes) seperated by a `,` with a closing `)` at the end.
Ex: `(c, e, g)` `(c#, f, g#, c#+1)`

There are many frequently used chords and to avoid requiring these definitions in
each source a standard chord set is virtually declared in the head of the source.
This means that the developer is free to use and if desired, *redefine* them.
All of the predefined chords match the pattern `[A-G] ( 's' | 'b' )? ( 'u' | 'd' [0-9]+ )? ( [a-z0-9]+ )?`
which can be found in the [Chord](src/main/java/cas/cs4tb3/mellowd/primitives/Chord.html) class.
The chord names begin with a capital letter from `A` to `G` followed by an optional
`s` or `b` to make the chord *sharp* or *flat* respectively. This defines the root note of
the chord which can be shifted up (`u`) or down (`d`) a number (`[0-9]+`) of octaves. The root
description is then transformed into a chord based on the last identifier. 
 
These chord creation identifiers include:

| Identifier | Chord            |
|------------|-----------------:|
| *nothing*  | major            |
| maj        | major            |
| m          | minor            |
| min        | minor            |
| aug        | augmented        |
| dim        | diminished       |
| dim7       | diminished 7th   |
| maj7b5     | major 7th flat 5 |
| maj7s5     | major 7th sharp 5|
| min7       | minor 7th        |
| minmaj7    | minor major 7th  |
| dom7       | dominant 7th     |
| 7          | major 7th        |
| maj7       | major 7th        |
| aug7       | augmented 7th    |

See the [Chord](src/main/java/cas/cs4tb3/mellowd/primitives/Chord.html#triads) class for more
information about the intervals in each chord.

Ex: `C` `Ds` `Ebu3` `Cm` `Ab7` `Bu1dom7`

__Chord indexing__ can be used to pull a note out of a chord. This only works on chord variables
as if the chord is being defined in place there is no need to index the chord, just use the exact
pitch at that position. To index a chord follow a chord variable name with a `:` and a number (`[0-9]+`).
The number is the index of the pitch to reference. __Indexing starts at 0__.

Ex: if `myChord -> (c, e, g)` is declared, `myChord:0` will resolve to `c`, `myChord:2` will
resolve to `g`.

Melodies
--------

A melody is a sequence of [notes](#notes) or [chords](#chords) that are preformed sequentially. 
Each element in a melody can be [articulated](#articulation). A melody is a declared in a
Mellow D source as a list of 1 or more melody parameters separated by `,`, an opening `[` and
a closing `]`.

Ex: `[a, b$, c#]` `[a!, (c, e, g)~, e-1.]` `[C:0, C:1, C:2]` `[a, *, b, *]`

Rhythms
-------

A rhythm is a collection of [beats](#beat) that can be understood as how the song
would sound if the only sound was a tapping sound. A rhythm is combined with a [melody](#melodies)
or [chord](#chords) to create a [phrase](#phrases). In a Mellow D source it is declared as a `,`
separated list of [beats](#beat) with an opening `<` and closing `>`.
 
Ex: `<w, h, q, e, s>` `<q., q>`

Slurring notes in Mellow D is the equivalent of applying a tenuto (`_`) [articulation](#articulation)
to each note matched with the slurred beat. It results in a nice gliding sound between sounds. In
a rhythm declaration slurred notes are wrapped in `(` and `)`.

Ex: `<q, (q, q, q)>` the last 3 quarter notes `q` are slurred.

__Tuplets__ (most commonly a triplet) alter the duration of a beat in a negative way. See the
[Beat](src/main/java/cas/cs4tb3/mellowd/Beat.html) class for an in-depth explanation of how
beat durations inside a tuplet are calculated. Tuplets can be declared as simply a number (`[0-9]+`)
before a beat or a number (`[0-9]+`) `:` a number (`[0-9]+`). If the (`:[0-9]+`) are excluded it is
implied to be __1__ less than the given number. If the first number is named *num* and the second *div*
the tuplet preforms *num* of the following beat in the time normally taken to preform *div* of the
following beat. 

For example a quarter note triplet (`3q`) would play 3 quarter notes in the time
normally taken to play 2. A `5:3q` tuplet would preform 5 quarter notes in the time normally taken
to preform 3.

Tuplets can be slurred as well by wrapping the tuplet beat with `(` and `)`.

Ex: `<q, q, 5e>` `<3(q), h>` `<7:4e, q, 3(e)>`are all 4 beats long

Phrases
-------

A phrase is a structure that can be played. It, like the other complex elements can be declared as
a [variable](#variables). A phrase is constructed as the product of a [melody](#melodies) or [chord](#chords)
with a [rhythm](#rhythms) with the `*` operator. A phrase is a sequence of playable sounds such that each
sound has a duration. The length of this sequence is determined by the longest of rhythm or pitch component
by repeating the shorter element until the length matches the length of the longer element.

For example: 

    + `[a, b, c]*<e>` will preform 3 sounds each for an eight note
    + `[a, b]*<e, e, q>` will preform 3 sounds: an eight note *a*, an eight note *a* and a quarter note *a*
    + `[a, b, c]*<3q, h>` will preform 4 sounds: the *a*, *b*, *c* triplet followed by a half note *a*.
    + `Cminmaj7*<q, e, e, q>` will preform 4 sounds: the Cminmaj7 chord for a quarter, eight, eight, quarter note.

Top Level Structures
====================
At the top level of the Mellow D source only 2 structures are accepted, [variable declarations](#variables)
and [blocks](#blocks). The majority of the source is taken up with blocks as these are
where the the song is declared.

Variables
---------

Variables can be very beneficial in reducing the amount of repetitive sound or rhythm definitions. A
variable is a key value mapping with each variable having a dynamic type. The type is evaluated at
the time the value is needed and may change anytime. The source is evaluated from the top down and 
so the value of a variable is the most recent declaration above where the value is requested. Variable
definitions look like `identifier -> value` where value can be a [chord](#chords), [melody](#melodies),
[rhythm](#rhythms) or a [phrase](#phrases). A variable declaration with a `*` following the `->` results
in the value being compiled as if inside a [percussion](#percussion) block, `identifier ->* value`.

Variables declared will be shifted to the appropriate octave when their value is required. A variable
reference can be used anywhere that the value could be used. An identifier that points to a chord
can be used anywhere a chord could be declared. 

Blocks
------

Blocks are the core structure of Mellow D. A block can be thought of as a thread for those with
a programming background. Each block is a stream of sounds flowing to the MIDI synthesiser. Every block
is declared with a unique name. If a block with the same name as an existing block the contents are
appended to the original. All blocks start at the beginning of the song and their location in the
source does not affect when the contents are preformed, only the order of the fragments matters.

A block starts with an identifier followed by optional [block options](#block-options) followed by an
opening `{` and a closing `}`. Everything between the `{` and `}` is the contents. The contents can be
[dynamic declarations](#dynamics) or [phrases](#phrases). For example the left and right hand of a
piano piece with each hand playing concurrently:

```
leftHand[octave -> 3] {
    ff [c, e, g, c+1]*<q>
}

rightHand[octave -> 5] {
    f << [Cmaj, Gm, Em]*<q> ff
}

leftHand {
    f >> G*<e, e, h.> p
}
```

Block Options
-------------

Block options are a set of knobs the developer can turn to better control the [block](#block).
There are 2 types of block options: property style options and flag options.

__Property options__ look like a variable declaration, `identifier -> value` with the value being
either an identifier or a number. A list of accepted property options are the following:

| property                                              | value        | description |
|-------------------------------------------------------|:------------:|-------------|
|**instrument**                                         | id or number | set the instrument by MIDI number or name See [GeneralMidiInstrument](src/main/java/cas/cs4tb3/mellowd/midi/GeneralMidiInstrument.html)|
|**soundbank**                                          | number       | set the soundbank by MIDI number. This is only required in compilation for custom synths.|
|**octave**                                             | number       | set the relative base octave for all sounds declared in the block |
|**loop** or **repeat**                                 | number       | copy the entire contents of the block where this option is set an extra *value* times|
|**onchannel** or **samechannelas** or **sharechannel** | id           | let the track manager put this block on the same MIDI channel as the channel named *value*. MIDI is limited in the number of channels available so large sources may need to compact the blocks.|
|**channel**                                            | number       | directly request the channel this block is on. General this option should not be touched but is available if required.|

__Flag options__ are used for boolean options. They can be thought of as a switch. If the option
is given as `identifier` it turns the option on. If given as `-identifier` it turns the option off.
A list of accepted flag options are the following:

| property                   | description |
|----------------------------|-------------|
|**drums** or **percussion** | puts the block in (or out of) [percussion](#percussion) mode|

Block options appear after a block name and, other than **loop** and **repeat**, carry throughout
each fragment. The options start with an opening `[`, end with a closing `]`, and are filled with
a list of property or flag options separated with a `,`. 

For example: `[percussion, octave -> 5, sharechannel -> myOtherBlock]` or `[instrument -> chimes, -drums]`

