//Dynamic
//=======

package org.mellowd.primitives;

import org.mellowd.midi.GeneralMidiConstants;

//The `Dynamic` changes the volume. In MIDI this translates to the velocity with which the
//note is played. It works out very nicely because to play a piano sound louder the player
//would hit the key "harder" (which really corresponds to faster) producing a louder sound
//but also giving the note more power. This is exactly how MIDI velocity works.
public final class Dynamic {
    private static final Dynamic[] DYNAMICS = new Dynamic[GeneralMidiConstants.MIN_VELOCITY + GeneralMidiConstants.MAX_VELOCITY + 1];
    static {
        for (int velocity = GeneralMidiConstants.MIN_VELOCITY; velocity < DYNAMICS.length; velocity++)
            DYNAMICS[velocity] = new Dynamic(velocity);
    }

    //Each dynamic has an associated MIDI velocity, for example `pppp` (pianissimo) corresponds
    //to the MIDI velocity `8`.
    public static final Dynamic pppp = getDynamic(8);
    public static final Dynamic ppp  = getDynamic(20);
    public static final Dynamic pp   = getDynamic(31);
    public static final Dynamic p    = getDynamic(42);
    public static final Dynamic mp   = getDynamic(53);
    public static final Dynamic mf   = getDynamic(64);
    public static final Dynamic f    = getDynamic(80);
    public static final Dynamic ff   = getDynamic(96);
    public static final Dynamic fff  = getDynamic(112);
    public static final Dynamic ffff = getDynamic(127);

    private final int velocity;

    private Dynamic(int velocity) {
        this.velocity = velocity;
    }

    public static Dynamic getDynamic(int velocity) {
        //Clip the velocity so it remains in bounds
        velocity = Math.max(GeneralMidiConstants.MIN_VELOCITY, Math.min(GeneralMidiConstants.MAX_VELOCITY, velocity));
        return DYNAMICS[velocity];
    }

    //`getDynamic` returns the MIDI velocity for this dynamic
    public int getVelocity() {
        return this.velocity;
    }

    public Dynamic louder(int velocityGrowth) {
        return getDynamic(this.velocity + velocityGrowth);
    }

    public Dynamic softer(int velocityDecline) {
        return getDynamic(this.velocity - velocityDecline);
    }
}
