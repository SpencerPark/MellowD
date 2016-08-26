package cas.cs4tb3.mellowd.intermediate.functions;

import java.util.*;

public class FunctionBank {
    public static class PercussionPair {
        private static final int PERCUSSION_INDEX = 0;
        private static final int HARMONIC_INDEX = 1;

        private final Function[] functions = new Function[2];

        public Function getPercussionVariant() {
            return functions[PERCUSSION_INDEX];
        }

        public Function getHarmonicVariant() {
            return functions[HARMONIC_INDEX];
        }

        public Function getPreferredVariant(boolean percussion) {
            if (percussion) {
                if (functions[PERCUSSION_INDEX] != null) return functions[PERCUSSION_INDEX];
                else                                     return functions[HARMONIC_INDEX];
            } else {
                if (functions[HARMONIC_INDEX] != null) return functions[HARMONIC_INDEX];
                else                                   return functions[PERCUSSION_INDEX];
            }
        }

        private boolean putFunction(Function function) {
            boolean overwritten;
            if (function.isPercussion()) {
                overwritten = functions[PERCUSSION_INDEX] != null;
                functions[PERCUSSION_INDEX] = function;
            } else {
                overwritten = functions[HARMONIC_INDEX] != null;
                functions[HARMONIC_INDEX] = function;
            }
            return overwritten;
        }
    }
    private final Map<FunctionSignature, PercussionPair> functionSignatures;

    public FunctionBank() {
        this.functionSignatures = new HashMap<>();
    }

    public boolean addFunction(Function function) {
        PercussionPair pair = this.functionSignatures.get(function.getSignature());
        if (pair == null) {
            pair = new PercussionPair();
            this.functionSignatures.put(function.getSignature(), pair);
        }

        return pair.putFunction(function);
    }

    public PercussionPair[] resolve(String name, Argument<?>... args) {
        Set<FunctionSignature> possible = new LinkedHashSet<>();
        this.functionSignatures.forEach((sig, fun) -> {
            if (sig.getName().equals(name)) {
                if (sig.getParameters().minSize() <= args.length && args.length <= sig.getParameters().size())
                    possible.add(sig);
            }
        });

        int position = 0;
        for (Argument<?> arg : args) {
            Iterator<FunctionSignature> itr = possible.iterator();
            while (itr.hasNext()) {
                FunctionSignature next = itr.next();
                if (arg.isNamed())
                    if (next.getParameters().getParam(arg.getName()) == null) itr.remove();
                else
                    if (next.getParameters().getParam(position) == null) itr.remove();
            }
            position++;
        }

        PercussionPair[] results = new PercussionPair[possible.size()];
        int index = 0;
        for (FunctionSignature signature : possible)
            results[index++] = this.functionSignatures.get(signature);
        return results;
    }
}
