package net.ninjacat.mk54.exceptions;

public class ClassCreationException extends MkCompilationException {


    public ClassCreationException(final String message) {
        super(message);
    }

    public ClassCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
