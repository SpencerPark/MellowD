//Articulation
//============

package org.mellowd.primitives;

//Articulation tweaks the performance of the sound. They can be applied to single pitches
//or chords.
public enum Articulation {
    //No articulation. This provides a way to specify no articulation avoiding null pointers.
    NONE,

    //`.`
    //Staccato makes the performance short and choppy. Described in jazz
    //as `dit`. To achieve this effect the duration will be chopped to a
    //third of its value and the note will be ended very quickly.
    STACCATO,

    //`!`
    //Staccatissimo makes the performance short but more powerful. It is
    //given some more emphasis. It is similar to staccato but the duration
    //is going to be chopped to a half (rather than a third) and it will be
    //played with a bit more velocity.
    STACCATISSIMO,

    //`^`
    //Marcato is the same a staccato but with more power. It is referred to
    //as `dhat` by jazz musicians and to preform a note with articulated with marcato
    //the note's duration will be chopped to a third, the velocity will be increased
    //and the note will be release very quickly.
    MARCATO,

    //`` ` ``
    //An accent is played by attacking the note. This gives it a much faster velocity and
    //will also drop off a bit quicker than the average note. This is sometimes referred to
    //as `dah` by jazz musicians.
    ACCENT,

    //`_`
    //Tenuto is the equivalent of a single note slur. It is also called `doo` by jazz musicians
    //and so in order to preform a tenuto note the note will be let off as slow as possible with
    //a slightly longer duration.
    TENUTO,

    //`~`
    //Gliscando is a glide. It can be preformed as a pitch bend. To preform a gliscando a total of
    //16 pitch bend changes will give the effect that the note is falling or climbing (depending
    //on the direction of bend). These changes will be equally spaced over the duration of the note
    //as to not interfere with the next note. Additionally a reset message will be queued for the
    //next note to take.
    //A gliscando on a chord will be performed as a *roll*. A quick melody made up of each note in
    //the chord in order.
    GLISCANDO;

    @Override
    public String toString() {
        switch (this) {
            case STACCATO:
                return ".";
            case STACCATISSIMO:
                return "!";
            case MARCATO:
                return "^";
            case ACCENT:
                return "`";
            case TENUTO:
                return "_";
            case GLISCANDO:
                return "~";
            case NONE:
            default:
                return "";
        }
    }
}
