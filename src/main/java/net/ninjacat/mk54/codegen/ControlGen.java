package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static net.ninjacat.mk54.codegen.CodeGenerator.JAVA_LANG_INTEGER;
import static org.objectweb.asm.Opcodes.*;

/**
 * Contains methods to generate byte code for control operations
 */
final class ControlGen {

    private static final String STACK_DESCRIPTOR = "Ljava/util/Stack;";
    private static final String JAVA_UTIL_STACK = "java/util/Stack";
    private static final String CALL_STACK = "callStack";
    static final String INDIRECT_JUMP_ADDRESS = "indirectJumpAddress";

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
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_INTEGER, "valueOf", "(I)Ljava/lang/Integer;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "push", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(POP);
        mv.visitJumpInsn(GOTO, subroutineLabel);
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

    /**
     * Returns generator for indirect goto operation
     *
     * @param register Register to use as address holder
     * @return Code generating function
     */
    static OperationCodeGenerator indirectGoto(final int register) {
        return (mv, context) -> {
            modifyRegisterForIndirect(register, mv);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, register);
            mv.visitInsn(FALOAD);
            mv.visitInsn(F2I);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, INDIRECT_JUMP_ADDRESS, "I");
            mv.visitJumpInsn(GOTO, context.getTrampolineLabel());
        };
    }

    private static void modifyRegisterForIndirect(final int register, final MethodVisitor mv) {
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
    }

    /**
     * Returns generator for loop operation
     *
     * @param register Loop register
     * @return Loop generating function
     */
    static OperationCodeGenerator loop(final int register) {
        return (mv, context) -> {
            final int targetAddress = CodeGenUtil.parseAddress(context.nextOperation());
            final Label targetLabel = context.getLabelForAddress(targetAddress);

            // if memory[register] > 1
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, register);
            mv.visitInsn(FALOAD);
            mv.visitInsn(FCONST_1);
            mv.visitInsn(FCMPL);
            // stop loop if reached 1
            final Label stopLoop = new Label();
            mv.visitJumpInsn(IFLE, stopLoop);
            // decrement and then perform jump
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, register);
            mv.visitInsn(DUP2);
            mv.visitInsn(FALOAD);
            mv.visitInsn(FCONST_1);
            mv.visitInsn(FSUB);
            mv.visitInsn(FASTORE);
            // jump to loop address
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitJumpInsn(GOTO, targetLabel);

            mv.visitLabel(stopLoop);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        };
    }

    /**
     * Returns generator for indirect subroutine call operation
     *
     * @param register Loop register
     * @return Loop generating function
     */
    static OperationCodeGenerator icall(final int register) {
        return (mv, context) -> {
            modifyRegisterForIndirect(register, mv);
            // save address from memory register into indirect jump address register
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, MEMORY, "[F");
            mv.visitIntInsn(BIPUSH, register);
            mv.visitInsn(FALOAD);
            mv.visitInsn(F2I);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, INDIRECT_JUMP_ADDRESS, "I");

            // store return address
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, CALL_STACK, STACK_DESCRIPTOR);
            mv.visitIntInsn(BIPUSH, context.getCurrentAddress() + 1);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_INTEGER, "valueOf", "(I)Ljava/lang/Integer;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, JAVA_UTIL_STACK, "push", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(POP);

            // jump to trampoline
            mv.visitJumpInsn(GOTO, context.getTrampolineLabel());
        };
    }
}
