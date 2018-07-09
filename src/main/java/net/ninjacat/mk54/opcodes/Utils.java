package net.ninjacat.mk54.opcodes;

import java.util.Locale;

/**
 * Utility functions to help with opcode processing
 */
public final class Utils {

    private static final Locale LOCALE = Locale.forLanguageTag("uk");
    ;

    private Utils() {
    }

    /**
     * Performs normalization of the mnemonic key:
     * <pre>
     *   - Converts to lowercase
     *   - Replaces cyrillic letter к with latin k
     *   - Replaces cyrillic х with latin x
     * </pre>
     *
     * @param key Operation mnemonic
     * @return Normalied mnemonic
     */
    static String normalizeKey(final String key) {
        return key.trim().toLowerCase(LOCALE).replaceAll("к", "k").replaceAll("х", "x");
    }
}
