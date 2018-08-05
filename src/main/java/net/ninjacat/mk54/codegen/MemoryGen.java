package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static org.objectweb.asm.Opcodes.*;


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
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, location);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.FASTORE);
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
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, location);
            mv.visitInsn(FALOAD);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        };
    }

    /**
     * Produces function that generates code to store value from X to memory location by address in memory register
     *
     * @param location Memory register number, 0 to E
     * @return Function that generates code for RECALL operation
     */
    static OperationCodeGenerator istore(final int location) {
        return (mv, context) -> {
            modifyRegisterForIndirect(location, mv);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, location);
            mv.visitInsn(FALOAD);
            mv.visitInsn(F2I);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(FASTORE);
        };
    }

    /**
     * Produces function that generates code to retrieve value from memory location by address in memory register into X
     *
     * @param location Memory register number, 0 to E
     * @return Function that generates code for RECALL operation
     */
    static OperationCodeGenerator irecall(final int location) {
        return (mv, context) -> {
            modifyRegisterForIndirect(location, mv);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, location);
            mv.visitInsn(FALOAD);
            mv.visitInsn(F2I);
            mv.visitInsn(FALOAD);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        };
    }
}
