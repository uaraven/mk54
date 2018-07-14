package net.ninjacat.mk54.exceptions;

class MkCompilationException extends RuntimeException {
    MkCompilationException(final String message) {
        super(message);
    }

    public MkCompilationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
