package cas.cs4tb3.mellowd.intermediate.functions.defaults;

import cas.cs4tb3.mellowd.intermediate.functions.Arguments;
import cas.cs4tb3.mellowd.intermediate.functions.Function;
import cas.cs4tb3.mellowd.intermediate.functions.IllegalArgumentException;
import cas.cs4tb3.mellowd.intermediate.functions.Parameter;
import cas.cs4tb3.mellowd.primitives.Chord;
import cas.cs4tb3.mellowd.primitives.Pitch;

public class IndexFunction extends Function<Pitch> {
    private static final Parameter<Chord> toIndex = Parameter.newRequiredParameter("toIndex", Chord.class);
    private static final Parameter<Integer> index = Parameter.newRequiredParameter("index", Integer.class);

    public IndexFunction() {
        super("index", toIndex, index);
    }

    @Override
    public Pitch evaluate(Arguments arguments) {
        Chord chord = arguments.get(toIndex);
        Integer i = arguments.get(index);

        if (i < 0)
            throw new IllegalArgumentException(index, "Cannot be negative but was "+i);
        if (i > chord.size())
            throw new IllegalArgumentException(index, "There are only "+chord.size()+" notes in the given chord but index was "+i);

        return chord.getPitchAt(i);
    }
}
