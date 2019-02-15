package org.mellowd.intermediate.functions;

import org.mellowd.intermediate.*;
import org.mellowd.intermediate.executable.expressions.Abstraction;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.NullMemory;
import org.mellowd.midi.GeneralMidiInstrument;
import org.mellowd.midi.Knob;
import org.mellowd.midi.MIDIControl;
import org.mellowd.midi.Pedal;

public class DefaultFunctions {
    /* ===========================================================
     *                   Instrument Change
     * ===========================================================
     */
    private static final QualifiedName INSTRUMENT_CHANGE_NAME = QualifiedName.ofUnqualified("instrument");
    private static final Abstraction INSTRUMENT_CHANGE;

    static {
        final Parameter<?> instrumentParam = Parameter.newRequiredParameter("instrument");
        final Parameter<Number> soundbankParam = Parameter.newOptionalParameter("soundbank", Number.class);

        Statement instrumentChangeBody = Statement.lift((env, out) -> {
            Object instrumentArg = instrumentParam.dereference(env.getMemory());
            Number soundbankArg = soundbankParam.dereference(env.getMemory());

            int instrument;
            if (instrumentArg instanceof Number) {
                instrument = ((Number) instrumentArg).intValue();
            } else if (instrumentArg instanceof String) {
                instrument = GeneralMidiInstrument.lookup((String) instrumentArg).midiNum();
            } else {
                throw new IllegalArgumentException("Wrong type. " + instrumentArg.getClass());
            }

            int soundbank = 0;
            if (soundbankArg != null) soundbank = soundbankArg.intValue();

            out.put(new InstrumentChange(instrument, soundbank));
        });

        INSTRUMENT_CHANGE = new Abstraction(new Parameters(instrumentParam, soundbankParam), false, instrumentChangeBody);
    }

    /* ===========================================================
     *                           Mute
     * ===========================================================
     */
    private static final QualifiedName MUTE_NAME = QualifiedName.ofUnqualified("mute");
    private static final Abstraction MUTE;

    static {
        final Parameter<Boolean> muteParam = Parameter.newRequiredParameter("mute", Boolean.class);

        Statement muteBody = Statement.lift((env, out) -> {
            Boolean mute = muteParam.dereference(env.getMemory());

            out.put(ChannelMute.getInstance(mute));
        });

        MUTE = new Abstraction(new Parameters(muteParam), false, muteBody);
    }

    /* ===========================================================
     *                      Octave Shift
     * ===========================================================
     */
    private static final QualifiedName OCTAVE_SHIFT_NAME = QualifiedName.ofUnqualified("octave");
    private static final Abstraction OCTAVE_SHIFT;

    static {
        final Parameter<Number> shiftAmtParam = Parameter.newRequiredParameter("shiftAmt", Number.class);

        Statement octaveShiftBody = Statement.lift((env, out) -> {
            Number shiftAmt = shiftAmtParam.dereference(env.getMemory());
            out.put(new OctaveShift(shiftAmt.intValue()));
        });
        // TODO throw exception if env is percussion? Calls automatically toggle to correct type

        OCTAVE_SHIFT = new Abstraction(new Parameters(shiftAmtParam), false, octaveShiftBody);
    }

    /* ===========================================================
     *                         Transpose
     * ===========================================================
     */
    private static final QualifiedName TRANSPOSE_NAME = QualifiedName.ofUnqualified("transpose");
    private static final Abstraction TRANSPOSE;

    static {
        final Parameter<Number> numSemiTones = Parameter.newRequiredParameter("transposeAmt", Number.class);

        Statement transposeBody = Statement.lift((env, out) -> {
            Number shiftAmt = numSemiTones.dereference(env.getMemory());
            out.put(new TransposeChange(shiftAmt.intValue()));
        });

        TRANSPOSE = new Abstraction(new Parameters(numSemiTones), false, transposeBody);
    }

    private static final QualifiedName TWIST_NAME = QualifiedName.ofUnqualified("twist");
    private static final Abstraction TWIST;

    static {
        final Parameter<String> knobParam = Parameter.newRequiredParameter("knob", String.class);
        final Parameter<Number> toParam = Parameter.newRequiredParameter("to", Number.class);

        TWIST = new Abstraction(
                new Parameters(knobParam, toParam), false,
                Statement.lift((env, out) -> {
                    String knob = knobParam.dereference(env.getMemory());
                    Number to = toParam.dereference(env.getMemory());

                    MIDIControl control = MIDIControl.getController(knob);
                    if (!control.getControllerType().equals(Knob.class))
                        throw new IllegalArgumentException(knob + " is not a knob.");

                    out.put(new MIDIKnobChange(control, to.intValue()));
                })
        );
    }

    private static final QualifiedName PRESS_NAME = QualifiedName.ofUnqualified("press");
    private static final Abstraction PRESS;
    private static final QualifiedName RELEASE_NAME = QualifiedName.ofUnqualified("release");
    private static final Abstraction RELEASE;

    static {
        final Parameter<String> pedalParam = Parameter.newRequiredParameter("pedal", String.class);
        final Parameter<Boolean> pressedParam = Parameter.newOptionalParameter("pressed", Boolean.class);

        PRESS = new Abstraction(
                new Parameters(pedalParam, pressedParam), false,
                Statement.lift((env, out) -> {
                    String pedal = pedalParam.dereference(env.getMemory());
                    Boolean pressed = pressedParam.dereference(env.getMemory());

                    MIDIControl control = MIDIControl.getController(pedal);
                    if (!control.getControllerType().equals(Pedal.class))
                        throw new IllegalArgumentException(pedal + " is not a pedal.");

                    out.put(new MIDIPedalChange(control, pressed != null ? pressed : true));
                })
        );

        RELEASE = new Abstraction(
                new Parameters(pedalParam), false,
                Statement.lift((env, out) -> {
                    String pedal = pedalParam.dereference(env.getMemory());

                    MIDIControl control = MIDIControl.getController(pedal);
                    if (!control.getControllerType().equals(Pedal.class))
                        throw new IllegalArgumentException(pedal + " is not a pedal.");

                    out.put(new MIDIPedalChange(control, false));
                })
        );
    }

    public static void addAllToScope(Memory scope) {
        scope.set(INSTRUMENT_CHANGE_NAME, new Closure(NullMemory.getInstance(), INSTRUMENT_CHANGE));
        scope.set(MUTE_NAME, new Closure(NullMemory.getInstance(), MUTE));
        scope.set(OCTAVE_SHIFT_NAME, new Closure(NullMemory.getInstance(), OCTAVE_SHIFT));
        scope.set(TRANSPOSE_NAME, new Closure(NullMemory.getInstance(), TRANSPOSE));
        scope.set(TWIST_NAME, new Closure(NullMemory.getInstance(), TWIST));
        scope.set(PRESS_NAME, new Closure(NullMemory.getInstance(), PRESS));
        scope.set(RELEASE_NAME, new Closure(NullMemory.getInstance(), RELEASE));
    }
}
