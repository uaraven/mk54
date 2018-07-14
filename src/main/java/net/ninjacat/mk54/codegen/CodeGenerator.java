package net.ninjacat.mk54.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.ninjacat.mk54.Mk54;
import net.ninjacat.mk54.exceptions.ClassCreationException;
import net.ninjacat.mk54.exceptions.UnknownOperationException;
import org.objectweb.asm.*;

import java.util.List;
import java.util.Map;

import static net.ninjacat.mk54.opcodes.Opcode.*;
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
    private static final String CLASS_NAME = "net/ninjacat/mk54/Mk54";
    private static final String CLASS_DESCRIPTOR = "L" + CLASS_NAME + ";";

    private static final Map<String, OperationCodeGenerator> OPERATION_CODEGEN = ImmutableMap.<String, OperationCodeGenerator>builder()
            .put(DIGIT_0, digit(0))
            .put(DIGIT_1, digit(1))
            .put(DIGIT_2, digit(2))
            .put(DIGIT_3, digit(3))
            .put(DIGIT_4, digit(4))
            .put(DIGIT_5, digit(5))
            .put(DIGIT_6, digit(6))
            .put(DIGIT_7, digit(7))
            .put(DIGIT_8, digit(8))
            .put(DIGIT_9, digit(9))
            .put(DECIMAL_POINT, CodeGenerator::decimal)
            .put(NEG, CodeGenerator::changeSign)
            .put(EXP, CodeGenerator::startExponent)
            .put(ENTER, CodeGenerator::enterNumber)
            .put(CX, CodeGenerator::clearX)
            .build();

    CodeGenerator() {
        super();
    }

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     * <p>
     * TODO: Add another flag to check if new digit should reset register X first. This flag should be set by all operations except for digit entry
     * TODO: Figure out stack frames, current generated code fails on stack frame validation (consider switching to Java 5 target?)
     */
    private static OperationCodeGenerator digit(final int digit) {
        return (mv, context) -> {
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
        };
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
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Z, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_T, "F");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Z, "F");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Y, "F");
    }

    /**
     * Generates a label for each program step. This allows easy goto/conditional jmp implementation
     *
     * @param mv      {@link MethodVisitor} for generated method
     * @param context Code generating context
     */
    private static void generateOperandAddressLabel(final MethodVisitor mv, final CodeGenContext context) {
        final Label opLabel = context.getLabelForAddress(context.getCurrentAddress());
        mv.visitLabel(opLabel);
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

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, CLASS_NAME, "makeXRegister", "()V", false);
    }


    /**
     * Changes decimalFactor to 10 effectively switching entry mode to fractional part
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void decimal(final MethodVisitor mv, final CodeGenContext context) {
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

    private static void generateExecuteMethod(final List<String> operations, final ClassVisitor classWriter) {
        final MethodVisitor executeMethod = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "execute", "()V", null, null);
        executeMethod.visitCode();
        final Label startLabel = new Label();
        executeMethod.visitLabel(startLabel);

        // Prepare context
        final CodeGenContext context = new CodeGenContext(operations, classWriter, executeMethod);

        for (final String operation : operations) {
            generateOperandAddressLabel(executeMethod, context);
            if (OPERATION_CODEGEN.containsKey(operation)) {
                OPERATION_CODEGEN.get(operation).generate(executeMethod, context);
            } else {
                throw new UnknownOperationException(operation);
            }
        }

        executeMethod.visitInsn(Opcodes.RETURN);
        final Label finalLabel = new Label();
        executeMethod.visitLabel(finalLabel);
        executeMethod.visitLocalVariable("this", CLASS_DESCRIPTOR, null, startLabel, finalLabel, 0);
        executeMethod.visitMaxs(0, 0);
        executeMethod.visitEnd();

        classWriter.visitEnd();
    }

    byte[] compile(final String operationsStr) {
        final List<String> operations = ImmutableList.copyOf(operationsStr.split("\\s+"));

        // Set up ASM
        final ClassReader reader;
        try {
            reader = new ClassReader(Mk54.class.getName());
        } catch (final Exception ex) {
            throw new ClassCreationException("Failed to create class", ex);
        }
        final ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

        final ClassVisitor cv = new ClassVisitor(ASM5, classWriter) {
            @Override
            public void visitEnd() {
                generateExecuteMethod(operations, this.cv);
                super.visitEnd();
            }

        };

        reader.accept(cv, 0);

        return classWriter.toByteArray();

    }
}
