package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Register operations: number entry, swap, rot, etc.
 */
final class RegisterGen {

    public static final String NUMBER_ENTRY_ENABLED = "preventPushStack";

    private RegisterGen() {
    }

    /**
     * Enters number in register X cycling the stack
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void enterNumber(final MethodVisitor mv, final CodeGenContext context) {
        forcePushStack(mv);
        pushStack(mv);
        prepareXForReset(mv);
    }

    static void pushStack(final MethodVisitor mv) {
        final Label noPushStack = new Label();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, NUMBER_ENTRY_ENABLED, "Z");
        mv.visitJumpInsn(IFNE, noPushStack);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Z, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_T, "D");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Z, "D");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Y, "D");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, NUMBER_ENTRY_ENABLED, "Z");

        mv.visitLabel(noPushStack);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     * <p>
     */
    static OperationCodeGenerator digit(final int digit) {
        return (mv, context) -> {
            switchToNumberMode(mv);

            // check if we need to reset X before entering next digit
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, RESET_X, "Z");
            final Label noReset = new Label();
            mv.visitJumpInsn(IFEQ, noReset);

            pushStack(mv);

            // Clear X if resetX flag is set
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(DCONST_0);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_MANTISSA, "D");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ICONST_0);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_EXPONENT, "I");

            mv.visitLabel(noReset);
            mv.visitFrame(F_SAME, 0, null, 0, null);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, ENTRY_MODE, "I");
            final Label exponentEntryLabel = new Label();
            mv.visitJumpInsn(IFNE, exponentEntryLabel);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, digit);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "mantissaDigitEntry", "(I)V", false);
            final Label exitLabel = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, exitLabel);

            mv.visitLabel(exponentEntryLabel);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, digit);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "exponentDigitEntry", "(I)V", false);

            mv.visitLabel(exitLabel);
            mv.visitFrame(F_SAME, 0, null, 0, null);

            // do not reset X
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ICONST_0);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, RESET_X, "Z");

        };
    }

    /**
     * Generates code for sign negation
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void negateSign(final MethodVisitor mv, final CodeGenContext context) {
        // check what sign needs changing, either mantissa or exponent
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, ENTRY_MODE, "I");

        final Label exponentNegationBranch = new Label();
        mv.visitJumpInsn(IFNE, exponentNegationBranch);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "negateMantissa", "()V", false);
        final Label exitPoint = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, exitPoint);

        // change exponent sign - call negateExponent() method
        mv.visitLabel(exponentNegationBranch);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "negateExponent", "()V", false);

        mv.visitLabel(exitPoint);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Restores X register from X1 register
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void restoreX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X1, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "D");
    }

    /**
     * Changes decimalFactor to 10 effectively switching entry mode to fractional part
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void decimal(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");

        final Label alreadyInDecimal = new Label();
        mv.visitJumpInsn(IFNE, alreadyInDecimal);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");

        mv.visitLabel(alreadyInDecimal);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Clears X register
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void clearX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DCONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_MANTISSA, "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_EXPONENT, "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DCONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "D");
    }

    /**
     * Swaps X and Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void swapXy(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Y, "D");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "D");
    }


    /**
     * Rotates stack registers
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void rotate(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "D");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X1, "D");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "D");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "D");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Z, "D");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_Y, "D");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_T, "D");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_Z, "D");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X1, "D");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_T, "D");
    }

}
