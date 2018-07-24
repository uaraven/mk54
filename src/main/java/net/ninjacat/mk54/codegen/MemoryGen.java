package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;


final class MemoryGen {

    private static final String MEMORY = "memory";

    private MemoryGen() {
    }

    /**
     * Produces function that generates code to store value from X to memory location
     *
     * @param location Memory register number, 0 to E
     * @return Function that generates code for STORE operation
     */
    static OperationCodeGenerator storeToMemory(final int location) {
        return (mv, context) -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(Opcodes.BIPUSH, location);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.FASTORE);

            prepareXForReset(mv, context);
        };
    }

    /**
     * Produces function that generates code to retrieve value from memory location to register X
     *
     * @param location Memory register number, 0 to E
     * @return Function that generates code for RECALL operation
     */
    static OperationCodeGenerator recallFromMemory(final int location) {
        return (mv, context) -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(Opcodes.BIPUSH, location);
            mv.visitInsn(Opcodes.FALOAD);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

            prepareXForReset(mv, context);
        };
    }
}
