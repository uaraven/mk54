package net.ninjacat.mk54.opcodes;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility functions to help with opcode processing
 */
final class Utils {

    private static final Locale LOCALE = Locale.forLanguageTag("uk");
    private static final Pattern SPACE = Pattern.compile("\\s+");

    private Utils() {
    }

    /**
     * Performs normalization of the mnemonic key:
     * <pre>
     *   - Converts to lowercase
     *   - Replaces cyrillic letter к with latin k
     *   - Replaces cyrillic х with latin x
     *   - Removes all whitespace from command, so sto 0 and sto0 will be treated the same
     * </pre>
     *
     * @param key Operation mnemonic
     * @return Normalied mnemonic
     */
    static String normalizeKey(final String key) {
        return SPACE.matcher(
                key.trim().toLowerCase(LOCALE)
                        .replaceAll("к", "k")
                        .replaceAll("х", "x"))
                .replaceAll("");
    }
}
