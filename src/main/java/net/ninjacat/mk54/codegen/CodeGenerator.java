package net.ninjacat.mk54.codegen;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

/**
 * Generates Java byte code for MK program
 */
class CodeGenerator {

    private static final String REGISTER_X = "x";
    private static final String REGISTER_Z = "z";
    private static final String REGISTER_T = "t";
    private static final String REGISTER_Y = "y";

    private static final String ENTRY_MODE = "entryMode";
    private static final String DECIMAL_FACTOR = "decimalFactor";
    private static final float ORDER_MULTIPLIER = 10f;
    private static final String CLASS_NAME = "Mk54";

    Map<String, OperationCodeGenerator> OPERATION_CODEGEN = ImmutableMap.<String, OperationCodeGenerator>builder()
            .put("00", numberGenerator(0))
            .put("01", numberGenerator(1))
            .put("02", numberGenerator(2))
            .put("03", numberGenerator(3))
            .put("04", numberGenerator(4))
            .put("05", numberGenerator(5))
            .put("06", numberGenerator(6))
            .put("07", numberGenerator(7))
            .put("08", numberGenerator(8))
            .put("09", numberGenerator(9))
            .put("0A", CodeGenerator::decimal)
            .put("0B", CodeGenerator::changeSign)
            .put("0E", CodeGenerator::enterNumber)
            .put("0D", CodeGenerator::clearX)
            .build();

    /**
     * Generates a label for each program step. This allows easy goto/conditional jmp implementation
     *
     * @param mv      {@link MethodVisitor} for generated method
     * @param context Code generating context
     */
    public static void generateOperandAddressLabel(final MethodVisitor mv, final CodeGenContext context) {
        final Label startLabel = context.addLabelForAddress(context.getCurrentAddress());
        mv.visitLabel(startLabel);
    }

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     *
     * TODO: Add another flag to check if new digit should reset register X first. This flag should be set by all operations except for digit entry
     */
    private static OperationCodeGenerator numberGenerator(final int digit) {
        return (mv, context) -> {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, ENTRY_MODE, "I");
            final Label entryModeExp = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, entryModeExp);
            // Adding digit to integer part of mantissa
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
            final Label decimalEntry = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, decimalEntry);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitLdcInsn(ORDER_MULTIPLIER);
            mv.visitInsn(Opcodes.FMUL);
            mv.visitLdcInsn((float) digit);
            mv.visitInsn(Opcodes.FADD);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            final Label exitPoint = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, exitPoint);
            mv.visitLabel(decimalEntry);
            // Adding digit to fractional part of mantissa
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.DUP);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitIntInsn(Opcodes.BIPUSH, digit);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
            mv.visitInsn(Opcodes.IDIV);
            mv.visitInsn(Opcodes.I2F);
            mv.visitInsn(Opcodes.FADD);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.DUP);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
            mv.visitIntInsn(Opcodes.BIPUSH, 10);
            mv.visitInsn(Opcodes.IDIV);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
            mv.visitJumpInsn(Opcodes.GOTO, exitPoint);
            mv.visitLabel(entryModeExp);
            // Adding digit to exponent - calling exponentDigitEntry() method
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitIntInsn(Opcodes.BIPUSH, digit);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_NAME, "exponentDigitEntry", "(I)V", false);
            mv.visitLabel(exitPoint);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        };
    }

    /**
     * Changes decimalFactor to 10 effectively switching entry mode to fractional part
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void decimal(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitIntInsn(Opcodes.BIPUSH, 10);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
    }

    /**
     * Clears X register
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void clearX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(0f);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Enters number in register X cycling the stack
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void enterNumber(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Z, "F");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_T, "F");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_Z, "F");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_Y, "F");
    }

    /**
     * Generates code for sign negation
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void changeSign(final MethodVisitor mv, final CodeGenContext context) {
        // check what sign needs changing, either mantissa or exponent
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, ENTRY_MODE, "I");

        final Label exponentNegationBranch = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, exponentNegationBranch);

        // change mantissa sign
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(Opcodes.FNEG);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        final Label exitPoint = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, exitPoint);

        // change exponent sign - call negateExponent() method
        mv.visitLabel(exponentNegationBranch);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_NAME, "negateExponent", "()V", false);

        mv.visitLabel(exitPoint);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
}
