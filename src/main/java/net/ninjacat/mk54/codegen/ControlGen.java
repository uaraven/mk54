package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static net.ninjacat.mk54.codegen.CodeGenUtil.CLASS_NAME;
import static org.objectweb.asm.Opcodes.*;

final class ControlGen {

    private static final String STACK_DESCRIPTOR = "Ljava/util/Stack;";
    private static final String JAVA_UTIL_STACK = "java/util/Stack";
    private static final String CALL_STACK = "callStack";

    private ControlGen() {
    }


    /**
     * Generates NOP byte code
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void nop(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitInsn(NOP);
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
        final int targetAddress = Integer.parseInt(context.nextOperation(), 16);
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitJumpInsn(GOTO, targetLabel);
    }

    /**
     * Performs subroutine call to an address in the next program step
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void gosub(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = Integer.parseInt(context.nextOperation(), 16);
        final Label subroutineLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
        mv.visitIntInsn(BIPUSH, context.getCurrentAddress() + 1);
        mv.visitMethodInsn(INVOKESTATIC, CodeGenerator.JAVA_LANG_INTEGER, "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "push", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(POP);
        mv.visitJumpInsn(GOTO, subroutineLabel);
    }

    static void returnFromSub(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "pop", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, CodeGenerator.JAVA_LANG_INTEGER);
        mv.visitMethodInsn(INVOKEVIRTUAL, CodeGenerator.JAVA_LANG_INTEGER, "intValue", "()I", false);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, "indirectJumpAddress", "I");
        mv.visitJumpInsn(GOTO, context.getTrampolineLabel());
    }

}
