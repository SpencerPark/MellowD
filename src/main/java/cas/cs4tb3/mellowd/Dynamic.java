//Dynamic
//=======

package cas.cs4tb3.mellowd;

import java.util.List;

//The `Dynamic` changes the volume. In MIDI this translates to the velocity with which the
//note is played. It works out very nicely because to play a piano sound louder the player
//would hit the key "harder" (which really corresponds to faster) producing a louder sound
//but also giving the note more power. This is exactly how MIDI velocity works.
public enum Dynamic {
    //Each dynamic has an associated MIDI velocity, for example `pppp` (pianissimo) corresponds
    //to the MIDI velocity `8`.
    pppp(8 ),
    ppp (20),
    pp  (31),
    p   (42),
    mp  (53),
    mf  (64),
    f   (80),
    ff  (96),
    fff (112),
    ffff(127);

    private int velocity;

    Dynamic(int velocity) {
        this.velocity = velocity;
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
}
