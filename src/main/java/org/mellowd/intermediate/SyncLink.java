package org.mellowd.intermediate;

import java.util.BitSet;
import java.util.Set;

public class SyncLink {
    //This is the object that will block all the threads until the
    //sync is complete
    private final Object lock = new Object();

    private final Output[] toSync;
    private final BitSet flags;
    private volatile long syncTime = -1;

    public SyncLink(Set<? extends Output> toSync) {
        if (toSync.size() < 2)
            throw new IllegalArgumentException("At least 2 channels need to be present to create a sync link.");

        this.toSync = toSync.toArray(new Output[toSync.size()]);
        this.flags = new BitSet(toSync.size());
    }

    /**
     * This method raises the flag for the given output and will
     * block until all outputs in the link have raised their flags.
     * When the flags have been raised a {@link LeapInTime} will be
     * added if necessary to sync the given output.<br>
     *
     * If the {@code output} is not present in the link this method will simply
     * return immediately.
     * @param output the output that is ready to sync
     */
    public void sync(Output output) throws InterruptedException {
        synchronized (this) {
            boolean flagRaised = false;
            for (int index = 0; index < toSync.length; index++) {
                if (toSync[index].equals(output)) {
                    this.flags.set(index);
                    flagRaised = true;
                    break;
                }
            }
            if (!flagRaised) return;
        }

        if (this.flags.cardinality() == this.toSync.length) {
            //This was the last flag to raise, release the hounds
            calcSyncStateTime();
            long leap = this.syncTime - output.getStateTime();
            if (leap > 0)
                output.put(new LeapInTime(this.syncTime - output.getStateTime()));
            synchronized (lock) {
                lock.notifyAll();
            }
            reset();
        } else {
            synchronized (lock) {
                lock.wait();
            }
            long leap = this.syncTime - output.getStateTime();
            if (leap > 0)
                output.put(new LeapInTime(this.syncTime - output.getStateTime()));
        }
    }

    private void calcSyncStateTime() {
        long time = -1;
        for (Output output : this.toSync) {
            time = Math.max(time, output.getStateTime());
        }
        this.syncTime = time;
    }

    private void reset() {
        this.flags.clear();
    }
}
