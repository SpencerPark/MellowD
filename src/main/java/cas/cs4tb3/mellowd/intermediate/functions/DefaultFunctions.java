package cas.cs4tb3.mellowd.intermediate.functions;

import cas.cs4tb3.mellowd.intermediate.ChannelMute;
import cas.cs4tb3.mellowd.intermediate.InstrumentChange;
import cas.cs4tb3.mellowd.intermediate.OctaveShift;
import cas.cs4tb3.mellowd.intermediate.TransposeChange;
import cas.cs4tb3.mellowd.intermediate.executable.statements.Statement;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;

public class DefaultFunctions {
    /* ===========================================================
     *                   Instrument Change
     * ===========================================================
     */
    private static final Function instrumentChangeInstance;
    private static final Function instrumentChangePercussionInstance;

    static {
        final Parameter<?> instrumentParam = Parameter.newRequiredParameter("instrument");
        final Parameter<Number> soundbankParam = Parameter.newOptionalParameter("soundbank", Number.class);

        Statement instrumentChangeBody = (env, out) -> {
            Object instrumentArg = instrumentParam.getReference().dereference(env.getMemory());
            Number soundbankArg = soundbankParam.getReference().dereference(env.getMemory());

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
        };
        FunctionSignature instrumentChangeSignature = new FunctionSignature("instrument", instrumentParam, soundbankParam);

        instrumentChangeInstance = new Function(instrumentChangeSignature, false, instrumentChangeBody);
        instrumentChangePercussionInstance = new Function(instrumentChangeSignature, true, instrumentChangeBody);
    }

    public static Function getInstrumentChangeInstance(boolean percussion) {
        if (percussion) return instrumentChangePercussionInstance;
        return instrumentChangeInstance;
    }

    /* ===========================================================
     *                           Mute
     * ===========================================================
     */
    private static final Function muteInstance;
    private static final Function mutePercussionInstance;

    static {
        final Parameter<Boolean> muteParam = Parameter.newRequiredParameter("mute", Boolean.class);

        Statement muteBody = (env, out) -> {
            Boolean mute = muteParam.getReference().dereference(env.getMemory());
            out.put(ChannelMute.getInstance(mute));
        };
        FunctionSignature muteSignature = new FunctionSignature("mute", muteParam);

        muteInstance = new Function(muteSignature, false, muteBody);
        mutePercussionInstance = new Function(muteSignature, true, muteBody);
    }

    public static Function getMuteInstance(boolean percussion) {
        if (percussion) return mutePercussionInstance;
        return muteInstance;
    }

    /* ===========================================================
     *                      Octave Shift
     * ===========================================================
     */
    private static final Function octaveShiftInstance;
    private static final Function octaveShiftPercussionInstance;

    static {
        final Parameter<Number> shiftAmtParam = Parameter.newRequiredParameter("shiftAmt", Number.class);

        FunctionSignature octaveShiftSignature = new FunctionSignature("octave", shiftAmtParam);

        octaveShiftInstance = new Function(octaveShiftSignature, false, (env, out) -> {
            Number shiftAmt = shiftAmtParam.getReference().dereference(env.getMemory());
            out.put(new OctaveShift(shiftAmt.intValue()));
        });
        octaveShiftPercussionInstance = new Function(octaveShiftSignature, true, (env, out) -> {
            throw new IllegalStateException("Cannot shift the octave of a percussion sound.");
        });
    }

    public static Function getOctaveShiftInstance(boolean percussion) {
        if (percussion) return octaveShiftPercussionInstance;
        return octaveShiftInstance;
    }

    /* ===========================================================
     *                         Transpose
     * ===========================================================
     */
    private static final Function transposeInstance;
    private static final Function transposePercussionInstance;

    static {
        final Parameter<Number> numSemiTones = Parameter.newRequiredParameter("transposeAmt", Number.class);

        FunctionSignature transposeSignature = new FunctionSignature("transpose", numSemiTones);

        transposeInstance = new Function(transposeSignature, false, (env, out) -> {
            Number shiftAmt = numSemiTones.getReference().dereference(env.getMemory());
            out.put(new TransposeChange(shiftAmt.intValue()));
        });
        transposePercussionInstance = new Function(transposeSignature, true, (env, out) -> {
            throw new IllegalStateException("Cannot transpose a percussion sound.");
        });
    }

    public static Function getTransposeInstance(boolean percussion) {
        if (percussion) return transposePercussionInstance;
        return transposeInstance;
    }

    public static void addAllToFunctionBank(FunctionBank bank) {
        bank.addFunction(getInstrumentChangeInstance(true));
        bank.addFunction(getInstrumentChangeInstance(false));

        bank.addFunction(getMuteInstance(true));
        bank.addFunction(getMuteInstance(false));

        bank.addFunction(getOctaveShiftInstance(true));
        bank.addFunction(getOctaveShiftInstance(false));

        bank.addFunction(getTransposeInstance(true));
        bank.addFunction(getTransposeInstance(false));
    }
}
