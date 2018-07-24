package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

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

    /**
     * Performs unconditional jump to an address in the next program step
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void gotoAddr(final MethodVisitor mv, final CodeGenContext context) {
        final int address = Integer.parseInt(context.nextOperation(), 16);
        final Label label = context.getLabelForAddress(address);
        mv.visitJumpInsn(GOTO, label);
    }
}
