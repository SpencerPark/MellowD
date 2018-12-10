package org.mellowd.plugin.defaults;

import org.mellowd.compiler.MellowD;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.QualifiedName;
import org.mellowd.intermediate.executable.expressions.Abstraction;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.functions.Parameter;
import org.mellowd.intermediate.functions.Parameters;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.NullMemory;
import org.mellowd.plugin.MellowDPlugin;
import org.mellowd.primitives.Melody;
import org.mellowd.primitives.Pitch;

import java.util.ArrayList;
import java.util.List;

public class Bjorklund implements MellowDPlugin {
    private final Parameter<Melody> melodyParam;
    private final Parameter<Number> pulsesParam;
    private final Parameter<Number> stepsParam;

    private final QualifiedName bjorklundFunctionName;
    private final Abstraction bjorklundFunction;

    public Bjorklund() {
        this.melodyParam = Parameter.newRequiredParameter("melody", Melody.class);
        this.pulsesParam = Parameter.newRequiredParameter("pulses", Number.class);
        this.stepsParam = Parameter.newRequiredParameter("steps", Number.class);

        this.bjorklundFunctionName = QualifiedName.fromString("mellowd.euclid");
        this.bjorklundFunction = new Abstraction(
                new Parameters(this.melodyParam, this.pulsesParam, this.stepsParam), false,
                Statement.lift((env, out) -> {
                    Memory locals = env.getMemory();

                    Melody melodyMap = melodyParam.dereference(locals);
                    int pulses = pulsesParam.dereference(locals).intValue();
                    int steps = stepsParam.dereference(locals).intValue();

                    locals.set(Closure.RETURN_NAME, bjorklundAlgorithm(melodyMap, pulses, steps));
                })
        );
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
                else melody.append(Pitch.REST);
            }
        }
        for (int i = 0; i < numRemainder; i++)
            for (boolean play : remainder) {
                if (play) melody.append(melodyMap.getAtIndex(mapIndex++));
                else melody.append(Pitch.REST);
            }

        return melody;
    }

    @Override
    public void apply(MellowD mellowD) {
        Memory globals = mellowD.getGlobals();
        globals.set(this.bjorklundFunctionName, new Closure(NullMemory.getInstance(), this.bjorklundFunction));
    }
}
