package cas.cs4tb3.mellowd.intermediate.functions.defaults;

import cas.cs4tb3.mellowd.intermediate.InstrumentChange;
import cas.cs4tb3.mellowd.intermediate.functions.Function;
import cas.cs4tb3.mellowd.intermediate.functions.FunctionSignature;
import cas.cs4tb3.mellowd.intermediate.functions.Parameter;
import cas.cs4tb3.mellowd.midi.GeneralMidiInstrument;

public class InstrumentChangeFunction extends Function {
    private static final Parameter<?> instrumentParam = Parameter.newRequiredParameter("instrument");
    private static final Parameter<Number> soundbankParam = Parameter.newOptionalParameter("soundbank", Number.class);

    private static InstrumentChangeFunction instance;
    private static InstrumentChangeFunction percussionInstance;

    public static InstrumentChangeFunction getInstance(boolean percussion) {
        if (percussion) {
            if (percussionInstance == null)
                percussionInstance = new InstrumentChangeFunction(true);
            return percussionInstance;
        } else {
            if (instance == null)
                instance = new InstrumentChangeFunction(false);
            return instance;
        }
    }

    private InstrumentChangeFunction(boolean percussion) {
        super(new FunctionSignature("instrument", instrumentParam, soundbankParam), percussion, (env, out) -> {
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
        });
    }
}
