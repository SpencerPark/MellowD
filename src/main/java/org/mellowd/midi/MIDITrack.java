package org.mellowd.midi;

import javax.sound.midi.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

public class MIDITrack {
    private static int comapreEvents(MidiEvent left, MidiEvent right) {
        int timeCmp = Long.compare(left.getTick(), right.getTick());
        if (timeCmp != 0) return timeCmp;

        MidiMessage leftMsg = left.getMessage();
        MidiMessage rightMsg = right.getMessage();

        // If same, don't do the complicated comparisons
        if (leftMsg == rightMsg) return 0;

        // Try and get a quick status comparison to make the distinction
        int statusCmp = Integer.compare(leftMsg.getStatus(), rightMsg.getStatus());
        if (statusCmp != 0) return statusCmp;

        // Resort to the shorter message
        int lenCmp = Integer.compare(leftMsg.getLength(), rightMsg.getLength());
        if (lenCmp != 0) return lenCmp;

        // Worst case, compare contents
        byte[] leftData = leftMsg.getMessage();
        byte[] rightData = rightMsg.getMessage();
        for (int i = 0; i < leftData.length; i++) {
            int dataCmp = Byte.compare(leftData[i], rightData[i]);
            if (dataCmp != 0) return dataCmp;
        }

        // They are the same...
        return 0;
    }

    public static boolean isNotMeta(MidiEvent event) {
        return event.getMessage().getStatus() != MetaMessage.META;
    }

    public static boolean isNotMeta(MidiMessage msg) {
        return msg.getStatus() != MetaMessage.META;
    }

    private final String name;

    private final NavigableMap<Long, Collection<MidiEvent>> events;
    private final AtomicLong lastNonMetaMessageTime;

    public MIDITrack(String name) {
        this.name = name;

        this.events = new TreeMap<>(Long::compare);
        this.lastNonMetaMessageTime = new AtomicLong();
    }

    public String getName() {
        return name;
    }

    public Track toTrackInSequence(Sequence sequence) {
        Track track = sequence.createTrack();
        this.addAllToTrack(track);
        return track;
    }

    public void addAllToTrack(Track track) {
        this.forEach(track::add);
    }

    private synchronized void updateLastNonMetaMessageTime() {
        this.events.descendingMap().values().stream()
                .flatMap(e -> e.stream().filter(MIDITrack::isNotMeta))
                .findFirst()
                .ifPresent(e -> {
                    this.lastNonMetaMessageTime.set(e.getTick());
                });
    }

    public synchronized void add(MidiEvent event) {
        this.lastNonMetaMessageTime.updateAndGet(last ->
                MIDITrack.isNotMeta(event) && event.getTick() >= event.getTick()
                        ? event.getTick() : last);

        this.events.compute(event.getTick(), (time, events) -> {
            if (events == null)
                events = new LinkedList<>();
            events.add(event);
            return events;
        });
    }

    public synchronized void remove(MidiEvent event) {
        this.events.computeIfPresent(event.getTick(), (time, events) -> {
            if (events.remove(event) && MIDITrack.isNotMeta(event) && this.lastNonMetaMessageTime.get() >= time)
                this.updateLastNonMetaMessageTime();

            return events.isEmpty() ? null : events;
        });
    }

    public long lastNonMetaEventTime() {
        return this.lastNonMetaMessageTime.get();
    }

    public synchronized long endTimeStamp() {
        if (this.events.isEmpty())
            return 0;
        return this.events.lastKey();
    }

    public synchronized long startTimeStamp() {
        if (this.events.isEmpty())
            return 0;
        return this.events.firstKey();
    }

    public synchronized void forEach(Consumer<MidiEvent> consumer) {
        this.events.forEach((time, events) -> events.forEach(consumer));
    }

    // TODO should have alternatives, not necessarily all tracks are infinte

    // Loops indefinitely, stop is exclusive
    // Consumer accepts an absolute time stamp between start (inclusive) and stop (exclusive)
    public synchronized void forEachInRange(long start, long stop, ObjLongConsumer<MidiMessage> consumer) {
        long rangeDuration = stop - start;
        long trackDuration = this.endTimeStamp();
        if (trackDuration == 0)
            return;
        //System.out.printf("start: %d, stop: %d, rangeDur: %d, trackDur: %d%n", start, stop, rangeDuration, trackDuration);

        // |----------|----------|----------|----------|
        //                  ^----|----------|-^
        //                  5                 1
        // start=15, stop=31
        // vStart=5, vStop=1, with 1 full loop and overflow
        // vTrackStart=10

        long virtualStart = start % trackDuration;
        long virtualStop = stop % trackDuration;
        long virtualTrackStart = (start / trackDuration) * trackDuration;

        boolean overflows = (virtualStart + rangeDuration) >= trackDuration;
        //System.out.printf("virtualStart: %d, virtualStop: %d, virtualTrackStart: %d, overflows: %b%n", virtualStart, virtualStop, virtualTrackStart, overflows);


        // If doesn't overflow then simply iterate over all those values
        if (!overflows) {
            this.events.subMap(virtualStart, virtualStop).values().forEach(es ->
                    es.forEach(e -> consumer.accept(e.getMessage(), virtualTrackStart + e.getTick())));
            return;
        }

        // Consume the first segment
        this.events.tailMap(virtualStart).values().forEach(es ->
                es.forEach(e ->
                        consumer.accept(e.getMessage(), virtualTrackStart + e.getTick())));

        // Consume the looped segments
        int loops = (int) ((stop - start) / trackDuration);
        // from [1,loops] inclusive
        for (int loop = 1; loop <= loops; loop++) {
            long loopTrackStart = virtualTrackStart + (loop * trackDuration);
            this.events.values().forEach(es ->
                    es.forEach(e ->
                            consumer.accept(e.getMessage(), loopTrackStart + e.getTick())));
        }

        // Consume the tail segment
        long virtualLoopTailTrackStart = virtualTrackStart + ((1 + loops) * trackDuration);
        this.events.headMap(virtualStop).values().forEach(es ->
                es.forEach(e ->
                        consumer.accept(e.getMessage(), virtualLoopTailTrackStart + e.getTick())));
    }
}
