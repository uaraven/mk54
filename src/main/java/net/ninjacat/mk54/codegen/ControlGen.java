package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Contains methods to generate byte code for control operations
 */
final class ControlGen {

    private static final String STACK_DESCRIPTOR = "Ljava/util/Stack;";
    private static final String JAVA_UTIL_STACK = "java/util/Stack";
    private static final String CALL_STACK = "callStack";
    private static final String INDIRECT_JUMP_ADDRESS = "indirectJumpAddress";
    private static final String JAVA_LANG_INTEGER = "java/lang/Integer";

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
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitJumpInsn(GOTO, targetLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Performs subroutine call to an address in the next program step
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void gosub(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label subroutineLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
        mv.visitIntInsn(BIPUSH, context.getCurrentAddress() + 1);
        mv.visitMethodInsn(INVOKESTATIC, CodeGenerator.JAVA_LANG_INTEGER, "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "push", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(POP);
        mv.visitJumpInsn(GOTO, subroutineLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Generates code to return from subroutine. If return stack is empty, it will return to address 00
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void returnFromSub(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "isEmpty", "()Z", false);
        final Label hasAddressLabel = new Label();
        mv.visitJumpInsn(IFEQ, hasAddressLabel);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, INDIRECT_JUMP_ADDRESS, "I");
        final Label exitLabel = new Label();
        mv.visitJumpInsn(GOTO, exitLabel);
        mv.visitLabel(hasAddressLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "pop", "()Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, JAVA_LANG_INTEGER);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_LANG_INTEGER, "intValue", "()I", false);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, INDIRECT_JUMP_ADDRESS, "I");
        mv.visitLabel(exitLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitJumpInsn(GOTO, context.getTrampolineLabel());
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Performs conditional jump to an address in the next program step if value in register X is not 0
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void jnz(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(Opcodes.IFNE, targetLabel);
    }

    /**
     * Performs conditional jump to an address in the next program step if value in register X is 0
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void jz(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(Opcodes.IFEQ, targetLabel);
    }

    /**
     * Performs conditional jump to an address in the next program step if value in register X is less than 0
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void jltz(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(Opcodes.IFLT, targetLabel);
    }

    /**
     * Performs conditional jump to an address in the next program step if value in register X is greater than or equal to 0
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void jgez(final MethodVisitor mv, final CodeGenContext context) {
        final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
        final Label targetLabel = context.getLabelForAddress(targetAddress);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(Opcodes.IFGE, targetLabel);
    }


    static OperationCodeGenerator indirectGoto(final int register) {
        return (mv, context) -> {
            if (register >= 0 && register <= 3) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
                mv.visitIntInsn(BIPUSH, register);
                mv.visitInsn(DUP2);
                mv.visitInsn(FALOAD);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FSUB);
                mv.visitInsn(FASTORE);
            } else if (register >= 4 && register <= 6) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
                mv.visitIntInsn(BIPUSH, register);
                mv.visitInsn(DUP2);
                mv.visitInsn(FALOAD);
                mv.visitInsn(FCONST_1);
                mv.visitInsn(FADD);
                mv.visitInsn(FASTORE);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, register);
            mv.visitInsn(FALOAD);
            mv.visitInsn(F2I);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, INDIRECT_JUMP_ADDRESS, "I");
            mv.visitJumpInsn(GOTO, context.getTrampolineLabel());
            mv.visitFrame(F_SAME, 0, null, 0, null);
        };
    }

}
