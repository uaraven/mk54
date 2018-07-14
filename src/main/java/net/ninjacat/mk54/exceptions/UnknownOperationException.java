package net.ninjacat.mk54.exceptions;

public class UnknownOperationException extends MkCompilationException {
    public UnknownOperationException(final String opCode) {
        super("Unknown opcode: " + opCode);
    }
}
