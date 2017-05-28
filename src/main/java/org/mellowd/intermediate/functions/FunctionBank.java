package org.mellowd.intermediate.functions;

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
        PercussionPair pair = this.functionSignatures
                .computeIfAbsent(function.getSignature(), signature -> new PercussionPair());

        return pair.putFunction(function);
    }

    public PercussionPair[] resolve(String name, Argument<?>... args) {
        boolean isEmptyBraces = args.length == 0 || (args.length == 1 && args[0].isEmpty());

        Collection<FunctionSignature> possible;
        if (isEmptyBraces)
            possible = this.resolveEmptyBracesStrategy(name);
        else
            possible = this.resolveNotEmptyBracesStrategy(name, args);

        PercussionPair[] results = new PercussionPair[possible.size()];
        int index = 0;
        for (FunctionSignature signature : possible)
            results[index++] = this.functionSignatures.get(signature);
        return results;
    }

    //This strategy looks for all signatures with a min size of 0 and returns them
    //If the argument is meant to be a null first argument then functions with only optional
    //args will be caught. It will also catch functions with no arguments.
    private Collection<FunctionSignature> resolveEmptyBracesStrategy(String name) {
        Collection<FunctionSignature> possible = new LinkedList<>();
        Collection<FunctionSignature> noArgPossibilities = new LinkedList<>();
        this.functionSignatures.forEach((sig, fun) -> {
            if (sig.getName().equals(name)) {
                if (sig.getParameters().minSize() == 0)
                    possible.add(sig);
                if (sig.getParameters().size() == 0)
                    noArgPossibilities.add(sig);
            }
        });

        //Give preference to the no args
        if (noArgPossibilities.isEmpty())
            return possible;
        return noArgPossibilities;
    }

    private Collection<FunctionSignature> resolveNotEmptyBracesStrategy(String name, Argument<?>... args) {
        Collection<FunctionSignature> possible = new LinkedList<>();

        this.functionSignatures.forEach((sig, fun) -> {
            if (sig.getName().equals(name)) {
                int minSize = sig.getParameters().minSize();
                int maxSize = sig.getParameters().size();

                if (minSize <= args.length && args.length <= maxSize)
                    possible.add(sig);
            }
        });

        int position = 0;
        for (Argument<?> arg : args) {
            Iterator<FunctionSignature> itr = possible.iterator();
            while (itr.hasNext()) {
                Parameters next = itr.next().getParameters();

                if (arg.isNamed()) {
                    if (next.getParam(arg.getName()) == null)
                        itr.remove();
                } else {
                    if (next.getParam(position) == null)
                        itr.remove();
                }
            }
            position++;
        }

        return possible;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("\nFunctions: {\n");
        this.functionSignatures.forEach((sig, pair) -> {
            if (pair.getHarmonicVariant() != null)
                str.append("\t").append(sig).append("\n");
            if (pair.getPercussionVariant() != null)
                str.append("\t").append(sig).append("\n");
        });
        str.append("}");
        return str.toString();
    }
}
