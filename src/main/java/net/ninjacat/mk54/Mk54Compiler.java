package net.ninjacat.mk54;

import net.ninjacat.mk54.exceptions.UnknownKeyException;
import net.ninjacat.mk54.opcodes.Opcodes;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Compiles MK-series (and B3-34) mnemonic programs into "binary" presentation.
 */
public class Mk54Compiler {

    public static void main(final String[] args) {
        final Mk54Compiler compiler = new Mk54Compiler();

        //TODO: Parameter processing and I/O
    }


    public String compile(final String input) {
        final Opcodes opcodes = new Opcodes();

        final String[] keys = input.split("\n");
        return Arrays.stream(keys)
                .map(key -> opcodes.findOpcode(key).orElseThrow(() -> new UnknownKeyException(key)))
                .collect(Collectors.joining(" "));
    }

}
