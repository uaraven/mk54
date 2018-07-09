package net.ninjacat.mk54.exceptions;

import java.io.IOException;

/**
 * Unchecked wrapper for {@link IOException}
 */
public class RuntimeIOException extends RuntimeException {

    public RuntimeIOException(final IOException cause) {
        super(cause);
    }
}
