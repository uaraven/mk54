package net.ninjacat.mk54.codegen;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Code generation for math functions
 */
@SuppressWarnings("MagicNumber")
final class MathGen {

    private static final String LAST_RANDOM = "lastRandom";

    private MathGen() {
    }

    /**
     * Generates code for call of single-argument trigonometry function from Math class (sin/cos/tan)
     *
     * @param function Name of the function
     * @return {@link OperationCodeGenerator}
     */
    static OperationCodeGenerator generateTrig(final String function) {
        return (mv, context) -> {
            saveX(mv, context);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, "radGradDeg", "I");

            final Label rad = new Label();
            final Label grad = new Label();
            final Label deg = new Label();
            final Label defaultBlock = new Label();

            mv.visitTableSwitchInsn(0, 2, defaultBlock, rad, grad, deg);

            // radians
            mv.visitLabel(rad);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBlock);

            // degrees
            mv.visitLabel(deg);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "toRadians", "(D)D", false);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBlock);

            // grads
            mv.visitLabel(grad);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitLdcInsn(Math.PI);
            mv.visitInsn(Opcodes.DMUL);
            mv.visitLdcInsn(200.0);
            mv.visitInsn(Opcodes.DDIV);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

            mv.visitLabel(defaultBlock);
            mv.visitFrame(F_SAME, 0, null, 0, null);
        };
    }

    static OperationCodeGenerator generateArcTrig(final String function) {
        return (mv, context) -> {
            saveX(mv, context);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, "radGradDeg", "I");
            final Label rad = new Label();
            final Label deg = new Label();
            final Label grad = new Label();
            final Label defaultBranch = new Label();
            mv.visitTableSwitchInsn(0, 2, defaultBranch, rad, grad, deg);

            mv.visitLabel(rad);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBranch);

            mv.visitLabel(deg);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "toDegrees", "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBranch);

            mv.visitLabel(grad);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitLdcInsn(200.0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.DMUL);
            mv.visitLdcInsn(Math.PI);
            mv.visitInsn(Opcodes.DDIV);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

            mv.visitLabel(defaultBranch);
            mv.visitFrame(F_SAME, 0, null, 0, null);
        };
    }

    /**
     * Calculates natural logarithm of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void ln(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "log", "(D)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates log base 10 of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void log(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "log10", "(D)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates ten to power of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void tenToPowerX(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(10.0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "pow", "(DD)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates e to power of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void eToPowerX(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(Math.E);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "pow", "(DD)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates square root of X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void sqrt(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "sqrt", "(D)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Takes X to power 2
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void pow2(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(DUP);
        mv.visitInsn(FMUL);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates 1/X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void inv(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FDIV);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Calculates X^Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void xPowY(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.F2D);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitInsn(Opcodes.F2D);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, "pow", "(DD)D", false);
        mv.visitInsn(Opcodes.D2F);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

        stackDown(mv, context);
    }


    /**
     * Puts value of PI into X
     * <p>
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void pi(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        RegisterGen.pushStack(mv);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(Math.PI);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Adds X to Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void add(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FADD);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
    }

    /**
     * Multiplies X by Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void mul(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FMUL);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
    }

    /**
     * Divides Y by X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void div(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FDIV);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
    }

    /**
     * Subtracts X from Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void sub(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FSUB);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
    }

    /**
     * Absolute value of X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void abs(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "abs", "(F)F", false);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Determine sign of the value in X. Result is -1 if number is negative, 1 if it is positive or 0 if it is equal to 0
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void sign(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FCONST_0);
        mv.visitInsn(FCMPG);
        final Label positiveOrZeroBranch = new Label();
        mv.visitJumpInsn(IFGE, positiveOrZeroBranch);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(-1f);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        final Label exit = new Label();
        mv.visitJumpInsn(GOTO, exit);

        mv.visitLabel(positiveOrZeroBranch);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FCONST_0);
        mv.visitInsn(FCMPL);
        final Label zeroBranch = new Label();
        mv.visitJumpInsn(IFLE, zeroBranch);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_1);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitJumpInsn(GOTO, exit);

        mv.visitLabel(zeroBranch);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitLabel(exit);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

    /**
     * Truncates fractional part of the number
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void trunc(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");

        mv.visitInsn(F2I);
        mv.visitInsn(I2F);

        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }


    /**
     * Removes integer part of the number
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void frac(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2I);
        mv.visitInsn(I2F);

        mv.visitInsn(FSUB);

        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Finds maximal number of X and Y. Replicates a bug in MK-series where 0 is treated as largest number
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void max(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(Opcodes.FCMPL);
        final Label xzero = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, xzero);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(Opcodes.FCMPL);
        final Label compare = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, compare);

        mv.visitLabel(xzero);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        final Label exit = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, exit);

        mv.visitLabel(compare);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, "max", "(FF)F", false);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitLabel(exit);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }

    /**
     * Generates random number in 0..1 range.
     * <p>
     * PRN generator formula used in calculators
     * <p>
     * ξi+1 = {10(ξi + Y + X7)/3 + 0,404067},
     * <p>
     * where ξi+1 is next PRN,
     * ξi - previous PRN,
     * Y - modified number in register X (it is not clear how it is modified),
     * X7 - digit in 7th position of number in register X. positions were numbered from 1
     *
     * <p>
     * Yes, this is a very bad PRNG
     * </p>
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    static void rnd(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);

        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(10f);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitInsn(FADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, CLASS_NAME, "getSegment", "()F", false);
        mv.visitInsn(FADD);
        mv.visitInsn(FMUL);
        mv.visitLdcInsn(3.0f);
        mv.visitInsn(FDIV);
        mv.visitLdcInsn(0.404067f);
        mv.visitInsn(FADD);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitInsn(F2I);
        mv.visitInsn(I2F);
        mv.visitInsn(FSUB);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, LAST_RANDOM, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }


}