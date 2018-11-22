package org.mellowd.intermediate;

import org.mellowd.midi.TimingEnvironment;
import org.mellowd.primitives.Beat;
import org.mellowd.primitives.Dynamic;
import org.mellowd.midi.MIDIChannel;

public class GradualDynamicChange extends DynamicChange {
    private Dynamic end;
    private Beat changeDuration;
    private final Boolean isCrescendo;

    public GradualDynamicChange(Dynamic start, Dynamic end, Beat changeDuration) {
        super(start);
        this.end = end;
        this.changeDuration = changeDuration;
        this.isCrescendo = start.getVelocity() <= end.getVelocity();
    }

    public GradualDynamicChange(Dynamic start, boolean crescendo) {
        super(start);
        this.end = start; // Flat line by default until it is set later
        this.changeDuration = Beat.EIGHTH();
        this.isCrescendo = crescendo;
    }

    public Dynamic getStart() {
        return super.getDynamic();
    }

    public Dynamic getEnd() {
        return end;
    }

    public void setEnd(Dynamic end) {
        if (this.isCrescendo && super.getDynamic().getVelocity() > end.getVelocity())
            throw new IllegalArgumentException("Crescendo specified but end dynamic is softer than the start.");
        if (!this.isCrescendo && super.getDynamic().getVelocity() < end.getVelocity())
            throw new IllegalArgumentException("Decrescendo specified but end dynamic is louder than the start.");

        this.end = end;
    }

    public Beat getChangeDuration() {
        return changeDuration;
    }

    public void setChangeDuration(Beat changeDuration) {
        this.changeDuration = changeDuration;
    }

    public boolean isCrescendo() {
        return isCrescendo;
    }

    @Override
    public void play(MIDIChannel channel) {
        //Calculate the total duration of the change
        long totalDuration = channel.ticksInBeat(this.changeDuration);

        //We want a linear increase or drop from this volume to the `targetDynamic`.
        int velocityChange = this.end.getVelocity() - super.getDynamic().getVelocity();
        double changeSlope = velocityChange / (double) totalDuration;

        //Using the general equation of a line `y = mx+b` we have a function
        //to get the velocity of each sound in the sequence. `y` is the velocity,
        //`m` is the `changeSlope`, `x` is the endTimeStamp that have passed since the start
        //of the phrase and `b` is the starting velocity.
        long stateTimeStep = totalDuration / Math.abs(velocityChange);
        for (long stateTime = 0; stateTime < totalDuration; stateTime += stateTimeStep) {
            Dynamic velocity = Dynamic.getDynamic((int) ((changeSlope * stateTime) + super.getDynamic().getVelocity()));
            channel.doLater(stateTime, () -> channel.setDynamic(velocity));
        }

        channel.doLater(this.changeDuration, () -> channel.setDynamic(this.end));
    }


    @Override
    public long calculateDuration(TimingEnvironment env) {
        return 0;
    }
}
