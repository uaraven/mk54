package net.ninjacat.mk54.codegen;

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.Map;

public class CodeGenerator {

    public static final String REGISTER_X = "x";
    public static final String ENTRY_MODE = "entryMode";
    public static final String DECIMAL_FACTOR = "decimalFactor";
    public static final float ORDER_MULTIPLIER = 10f;
    private static final String CLASS_NAME = "Mk54";
    Map<String, OperationCodeGenerator> OPERATION_CODEGEN = ImmutableMap.<String, OperationCodeGenerator>builder()
            .put("00", numberGenerator(0))
            .put("01", numberGenerator(1))
            .put("02", numberGenerator(1))
            .put("03", numberGenerator(1))
            .put("04", numberGenerator(1))
            .put("05", numberGenerator(1))
            .put("06", numberGenerator(1))
            .put("07", numberGenerator(1))
            .put("08", numberGenerator(1))
            .put("09", numberGenerator(1))
            .put("0A", decimal())
            .put("0B", changeSign())
            .build();

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     */
    private static OperationCodeGenerator numberGenerator(final int digit) {
        return (mv, context) -> {
            // start label
            final Label l0 = new Label();
            mv.visitLabel(l0);
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
            mv.visitLdcInsn(10f);
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
            mv.visitInsn(Opcodes.RETURN);
            final Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitLocalVariable("this", "LMk54;", null, l0, l7, 0);
            mv.visitLocalVariable("n", "I", null, l0, l7, 1);
            mv.visitMaxs(4, 2);
        };
    }

    private OperationCodeGenerator decimal() {
        return null;
    }

    private OperationCodeGenerator changeSign() {
        return null;
    }
}
