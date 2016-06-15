//Dynamic
//=======

package cas.cs4tb3.mellowd;

import cas.cs4tb3.mellowd.midi.GeneralMidiConstants;

import java.util.List;

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

    private int velocity;

    private Dynamic(int velocity) {
        this.velocity = velocity;
    }

    public static Dynamic getDynamic(int velocity) {
        //Clip the velocity so it remains in bounds
        velocity = Math.max(GeneralMidiConstants.MIN_VELOCITY, Math.min(GeneralMidiConstants.MAX_VELOCITY, velocity));
        return DYNAMICS[velocity];
    }

    //`getVelocity` returns the MIDI velocity for this dynamic
    public int getVelocity() {
        return this.velocity;
    }

    //This method applies this dynamic to each sound in the `playableSounds`
    public void applyToAllSounds(PlayableSound[] playableSounds) {
        for (PlayableSound sound : playableSounds) {
            sound.setVelocity(this.getVelocity());
        }
    }

    public void graduallyApplyToAllSounds(Dynamic targetDynamic, TimingEnvironment timingEnv, List<PlayableSound> playableSounds) {
        //Calculate the total duration of the change
        long totalDuration = 0;
        for (PlayableSound sound : playableSounds) {
            totalDuration += timingEnv.ticksInBeat(sound.getDuration());
        }

        //We want a linear increase or drop from this volume to the `targetDynamic`.
        int velocityChange = targetDynamic.velocity - this.velocity;
        double changeSlope = velocityChange / (double) totalDuration;
        //Using the general equation of a line `y = mx+b` we have a function
        //to get the velocity of each sound in the sequence. `y` is the velocity,
        //`m` is the `changeSlope`, `x` is the ticks that have passed since the start
        //of the phrase and `b` is the starting velocity.
        long stateTime = 0;
        for (PlayableSound sound : playableSounds) {
            sound.setVelocity((int) (changeSlope*stateTime + this.velocity));
            stateTime += timingEnv.ticksInBeat(sound.getDuration());
        }
    }

    //TODO change all of these clip calls to dynamic grow and shrinks? maybe a different name
    //Clip the velocity to top or bottom out to the min or max if it is out of bounds.
    public static int clip(int velocity) {
        return Math.max(0, Math.min(velocity, 127));
    }


}
