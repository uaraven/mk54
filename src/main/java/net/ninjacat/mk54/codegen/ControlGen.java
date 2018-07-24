package net.ninjacat.mk54.codegen;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.RETURN;

final class ControlGen {

    private ControlGen() {
    }

    /**
     * Terminates program execution
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void startStop(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitInsn(RETURN);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

}
