package net.ninjacat.mk54.exceptions;

public class UnknownCommandException extends MkCompilationException {

    public UnknownCommandException(final String key) {
        super(String.format("Compilation failed, unknown operation: %s", key));
    }
}
