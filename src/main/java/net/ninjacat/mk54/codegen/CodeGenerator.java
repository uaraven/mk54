package net.ninjacat.mk54.codegen;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generates Java byte code for MK program
 */
class CodeGenerator {

    private static final String REGISTER_X = "x";
    private static final String REGISTER_Z = "z";
    private static final String REGISTER_T = "t";
    private static final String REGISTER_Y = "y";
    private static final String REGISTER_X_MANTISSA = "xMantissa";
    private static final String REGISTER_X_EXPONENT = "xExponent";


    private static final String ENTRY_MODE = "entryMode";
    private static final String DECIMAL_FACTOR = "decimalFactor";
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
            .put("0C", CodeGenerator::startExponent)
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
        final Label startLabel = context.getLabelForAddress(context.getCurrentAddress());
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
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, digit);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "mantissaDigitEntry", "(I)V", false);
        };
    }

    /**
     * Changes decimalFactor to 10 effectively switching entry mode to fractional part
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void decimal(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, 10);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, DECIMAL_FACTOR, "I");
    }

    /**
     * Switches digit entry mode to exponent
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void startExponent(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, ENTRY_MODE, "I");
    }


    /**
     * Clears X register
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void clearX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_MANTISSA, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_EXPONENT, "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_0);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
    }

    /**
     * Enters number in register X cycling the stack
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void enterNumber(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Z, "F");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_T, "F");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_Z, "F");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
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
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, ENTRY_MODE, "I");

        final Label exponentNegationBranch = new Label();
        mv.visitJumpInsn(Opcodes.IFNE, exponentNegationBranch);

        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_NAME, "negateMantissa", "()V", false);
        final Label exitPoint = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, exitPoint);

        // change exponent sign - call negateExponent() method
        mv.visitLabel(exponentNegationBranch);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CLASS_NAME, "negateExponent", "()V", false);

        mv.visitLabel(exitPoint);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    }
}
