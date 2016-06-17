package cas.cs4tb3.mellowd.intermediate;

import cas.cs4tb3.mellowd.Beat;
import cas.cs4tb3.mellowd.Dynamic;
import cas.cs4tb3.mellowd.midi.MIDIChannel;

/**
 * Created on 2016-06-15.
 */
public class GradualDynamicChange implements Playable {
    private Dynamic start;
    private Dynamic end;
    private Beat changeDuration;

    public GradualDynamicChange(Dynamic start, Dynamic end, Beat changeDuration) {
        this.start = start;
        this.end = end;
        this.changeDuration = changeDuration;
    }

    public GradualDynamicChange(Dynamic start) {
        this.start = start;
        this.end = start; //Flat line by default until it is set later
        this.changeDuration = Beat.EIGHTH;
    }

    public Dynamic getStart() {
        return start;
    }

    public void setStart(Dynamic start) {
        this.start = start;
    }

    public Dynamic getEnd() {
        return end;
    }

    public void setEnd(Dynamic end) {
        this.end = end;
    }

    public Beat getChangeDuration() {
        return changeDuration;
    }

    public void setChangeDuration(Beat changeDuration) {
        this.changeDuration = changeDuration;
    }

    @Override
    public void play(MIDIChannel channel) {
        //Calculate the total duration of the change
        long totalDuration = channel.ticksInBeat(this.changeDuration);

        //We want a linear increase or drop from this volume to the `targetDynamic`.
        int velocityChange = this.end.getVelocity() - this.start.getVelocity();
        double changeSlope = velocityChange / (double) totalDuration;

        //Using the general equation of a line `y = mx+b` we have a function
        //to get the velocity of each sound in the sequence. `y` is the velocity,
        //`m` is the `changeSlope`, `x` is the ticks that have passed since the start
        //of the phrase and `b` is the starting velocity.
        long stateTimeStep = totalDuration / Math.abs(velocityChange);
        for (long stateTime = 0; stateTime < totalDuration; stateTime += stateTimeStep) {
            Dynamic velocity = Dynamic.getDynamic((int) ((changeSlope * stateTime) + this.start.getVelocity()));
            channel.doLater(stateTime, () -> channel.setVelocity(velocity));
        }

        channel.doLater(this.changeDuration, () -> channel.setVelocity(this.end));
    }
}
