package net.ninjacat.mk54.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.ninjacat.mk54.Mk54;
import net.ninjacat.mk54.exceptions.ClassCreationException;
import net.ninjacat.mk54.exceptions.UnknownOperationException;
import org.objectweb.asm.*;

import java.util.List;
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
    private static final String CLASS_DESCRIPTOR = "L" + CLASS_NAME + ";";

    private static final Map<String, OperationCodeGenerator> OPERATION_CODEGEN = ImmutableMap.<String, OperationCodeGenerator>builder()
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

    private Label startLabel;

    CodeGenerator() {
        this.startLabel = null;
    }

    private static void generateConstructor(final ClassWriter classWriter) {
        final MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        final Label start = new Label();
        mv.visitLabel(start);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "initialize", "()V", false);
        mv.visitInsn(RETURN);
        final Label end = new Label();
        mv.visitLabel(end);
        mv.visitLocalVariable("this", CLASS_DESCRIPTOR, null, start, end, 0);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     * <p>
     * TODO: Add another flag to check if new digit should reset register X first. This flag should be set by all operations except for digit entry
     */
    private static OperationCodeGenerator numberGenerator(final int digit) {
        return (mv, context) -> {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, digit);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "mantissaDigitEntry", "(I)V", false);
        };
    }

    public byte[] compile(final String operationsStr) {
        final List<String> operations = ImmutableList.copyOf(operationsStr.split("\\s+"));

        // Set up ASM
        final ClassReader reader;
        try {
            reader = new ClassReader(Mk54.class.getName());
        } catch (final Exception ex) {
            throw new ClassCreationException("Failed to create class", ex);
        }
        final ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        final ClassVisitor cv = new ClassVisitor(ASM6, classWriter) {
            @Override
            public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, CLASS_NAME, null, "java/lang/Object", null);
            }

            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
                if ("execute".equals(name)) {
                    return generateExecuteMethod(operations, classWriter);
                } else {
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }
        };
        reader.accept(cv, 0);

        return classWriter.toByteArray();

    }

    private MethodVisitor generateExecuteMethod(final List<String> operations, final ClassWriter classWriter) {
        final MethodVisitor executeMethod = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "execute", "()V", null, null);
        executeMethod.visitCode();

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
        executeMethod.visitLocalVariable("this", CLASS_DESCRIPTOR, null, this.startLabel, finalLabel, 0);
        executeMethod.visitMaxs(0, 0);
        executeMethod.visitEnd();

        classWriter.visitEnd();

        return executeMethod;
    }

    /**
     * Generates a label for each program step. This allows easy goto/conditional jmp implementation
     *
     * @param mv      {@link MethodVisitor} for generated method
     * @param context Code generating context
     */
    private void generateOperandAddressLabel(final MethodVisitor mv, final CodeGenContext context) {
        final Label opLabel = context.getLabelForAddress(context.getCurrentAddress());
        if (this.startLabel == null) {
            this.startLabel = opLabel;
        }
        mv.visitLabel(opLabel);
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
