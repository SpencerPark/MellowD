package org.mellowd.midi;

/**
 * Created on 2016-06-14.
 */
public class MidiRuntimeException extends RuntimeException {
    public MidiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MidiRuntimeException(Throwable cause) {
        super(cause);
    }
}
