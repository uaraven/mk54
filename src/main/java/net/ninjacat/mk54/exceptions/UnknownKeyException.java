package net.ninjacat.mk54.exceptions;

public class UnknownKeyException extends MkCompilationException {

    public UnknownKeyException(final String key) {
        super(String.format("Compilation failed, unknown operation: %s", key));
    }
}
