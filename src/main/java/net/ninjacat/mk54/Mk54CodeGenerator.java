package net.ninjacat.mk54;

import net.ninjacat.mk54.exceptions.UnknownCommandException;
import net.ninjacat.mk54.opcodes.Opcodes;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Compiles MK-series (and B3-34) mnemonic programs into "binary" presentation.
 */
public class Mk54CodeGenerator {

    private static final Pattern ADDRESS = Pattern.compile("^(\\d{2}\\.).*");
    private final Opcodes opcodes;

    Mk54CodeGenerator() {
        this.opcodes = new Opcodes();
    }

    public static void main(final String[] args) {
        final Mk54CodeGenerator compiler = new Mk54CodeGenerator();

        //TODO: Parameter processing and I/O
    }

    private static String stripAddress(final String line) {
        final Matcher matcher = ADDRESS.matcher(line);
        if (matcher.matches()) {
            return line.substring(matcher.group(1).length()).trim();
        } else {
            return line.trim();
        }
    }

    /**
     * Converts program mnemonics into "binary" hex code
     *
     * @param input Program source
     * @return String containing hex codes of operations
     */
    public String compile(final String input) {
        final String[] keys = input.split("\n");
        return Arrays.stream(keys)
                .map(Mk54CodeGenerator::stripAddress)
                .map(key -> this.opcodes.findOpcode(key).orElseThrow(() -> new UnknownCommandException(key)))
                .collect(Collectors.joining(" "));
    }

}
