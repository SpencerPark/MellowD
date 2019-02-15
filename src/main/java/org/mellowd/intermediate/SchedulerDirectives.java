package org.mellowd.intermediate;

import org.mellowd.primitives.Beat;

public class SchedulerDirectives {
    public static class Quantize {
        private final int barSize;
        private final int barOffset;
        private final String blockRef;

        private Quantize(int barSize, int barOffset) {
            this.barSize = barSize;
            this.barOffset = barOffset;
            this.blockRef = null;
        }

        private Quantize(String blockRef) {
            this.barSize = -1;
            this.barOffset = 0;
            this.blockRef = blockRef;
        }

        public boolean isBlockAlignment() {
            return this.blockRef != null;
        }

        public int getBarSize() {
            return this.barSize;
        }

        public int getBarOffset() {
            return this.barOffset;
        }

        public String getBlockRef() {
            return this.blockRef;
        }
    }

    public static final class Align {
        private final boolean alignLeft;
        private final Beat offset;

        private Align(boolean alignLeft, Beat offset) {
            this.alignLeft = alignLeft;
            this.offset = offset;
        }

        public boolean isAlignedLeft() {
            return this.alignLeft;
        }

        public Beat getOffset() {
            return this.offset;
        }
    }

    public static final class Finite {
        private final int repetitions;
        private final boolean resume;
        private final SchedulerDirectives resumeDirectives;

        private Finite(int repetitions, boolean resume, SchedulerDirectives resumeDirectives) {
            this.repetitions = repetitions;
            this.resume = resume;
            this.resumeDirectives = resumeDirectives;
        }

        public int getRepetitions() {
            return this.repetitions;
        }

        public boolean shouldResume() {
            return this.resume;
        }

        public boolean hasResumeDirectives() {
            return this.resumeDirectives != null;
        }

        public SchedulerDirectives getResumeDirectives() {
            return this.resumeDirectives;
        }
    }

    public static Quantize quantize(int barSize) {
        return SchedulerDirectives.quantize(barSize, 0);
    }

    public static Quantize quantize(int barSize, int barOffset) {
        return new Quantize(barSize, barOffset);
    }

    public static Quantize alignBlock(String block) {
        return new Quantize(block);
    }

    public static Align alignRight() {
        return new Align(false, null);
    }

    public static Align alignRight(Beat padding) {
        return new Align(false, padding);
    }

    public static Align alignLeft() {
        return new Align(true, null);
    }

    public static Align alignLeft(Beat padding) {
        return new Align(true, padding);
    }

    public static Finite runNTimes(int n) {
        return new Finite(n, false, null);
    }

    public static Finite interruptNTimes(int n) {
        return new Finite(n, true, null);
    }

    public static Finite interruptNTimes(int n, SchedulerDirectives directives) {
        return new Finite(n, true, directives);
    }

    private final Quantize quantize;
    private final Align align;
    private final Finite finite;

    public SchedulerDirectives(Quantize quantize, Align align, Finite finite) {
        this.quantize = quantize;
        this.align = align;
        this.finite = finite;
    }

    public Quantize getQuantizeDirective() {
        return this.quantize;
    }

    public Align getPaddingDirective() {
        return this.align;
    }

    public Finite getFiniteDirective() {
        return this.finite;
    }
}
