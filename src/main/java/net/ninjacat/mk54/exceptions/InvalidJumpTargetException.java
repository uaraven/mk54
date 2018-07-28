package net.ninjacat.mk54.exceptions;

public class InvalidJumpTargetException extends MkCompilationException {
    public InvalidJumpTargetException(final String address) {
        super("Invalid target jump address: " + address);
    }
}
