package org.mellowd.plugin.defaults;

import org.mellowd.intermediate.functions.*;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.parser.MellowD;
import org.mellowd.plugin.MellowDPlugin;
import org.mellowd.primitives.Melody;
import org.mellowd.primitives.Pitch;

import java.util.ArrayList;
import java.util.List;

public class Bjorklund implements MellowDPlugin {
    private final Parameter<Melody> melodyParam;
    private final Parameter<Number> pulsesParam;
    private final Parameter<Number> stepsParam;

    private final Function bjorklundFunction;

    public Bjorklund() {
        this.melodyParam = Parameter.newRequiredParameter("melody", Melody.class);
        this.pulsesParam = Parameter.newRequiredParameter("pulses", Number.class);
        this.stepsParam = Parameter.newRequiredParameter("steps", Number.class);

        FunctionSignature signature = new FunctionSignature("euclid",
                melodyParam, pulsesParam, stepsParam);
        this.bjorklundFunction = new Function(signature, false, (env, out) -> {
            Memory locals = env.getMemory();
            Memory returnStorage  = env.getMemory(FunctionExecutionEnvironment.RETURN_QUALIFIER);

            Melody melodyMap = melodyParam.getReference().dereference(locals);
            int pulses = pulsesParam.getReference().dereference(locals).intValue();
            int steps = stepsParam.getReference().dereference(locals).intValue();

            returnStorage.set("default", bjorklundAlgorithm(melodyMap, pulses, steps));
        });
    }

    private Melody bjorklundAlgorithm(Melody melodyMap, int pulses, int steps) {
        List<Boolean> chunk = new ArrayList<>();
        chunk.add(true);

        List<Boolean> remainder = new ArrayList<>();
        remainder.add(false);

        int numChunks = pulses;
        int numRemainder = Math.abs(steps - pulses);
        while (numRemainder > 1) {
            List<Boolean> newChunk = new ArrayList<>(chunk);
            newChunk.addAll(remainder);

            if (numChunks >= numRemainder)
                remainder = chunk;
            chunk = newChunk;

            int newNumChunks = Math.min(numChunks, numRemainder);
            numRemainder = Math.abs(numChunks - numRemainder);
            numChunks = newNumChunks;
        }

        Melody melody = new Melody();
        int mapIndex = 0;
        for (int i = 0; i < numChunks; i++) {
            for (boolean play : chunk) {
                if (play) melody.append(melodyMap.getAtIndex(mapIndex++));
                else      melody.append(Pitch.REST);
            }
        }
        for (int i = 0; i < numRemainder; i++)
            for (boolean play : remainder) {
                if (play) melody.append(melodyMap.getAtIndex(mapIndex++));
                else      melody.append(Pitch.REST);
            }

        return melody;
    }

    @Override
    public void apply(MellowD mellowD) {
        FunctionBank functions = mellowD.getOrCreateFunctionBank("mellowd");
        functions.addFunction(this.bjorklundFunction);
    }
}
