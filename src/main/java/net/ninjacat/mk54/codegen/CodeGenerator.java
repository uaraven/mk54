package net.ninjacat.mk54.codegen;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.ninjacat.mk54.Mk54;
import net.ninjacat.mk54.exceptions.ClassCreationException;
import net.ninjacat.mk54.exceptions.UnknownOperationException;
import org.objectweb.asm.*;

import java.util.List;
import java.util.Map;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static net.ninjacat.mk54.opcodes.Opcode.SWAP;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates Java byte code for MK program
 */
class CodeGenerator {

    private static final String REGISTER_X = "x";
    private static final String REGISTER_X1 = "x1";
    private static final String REGISTER_Z = "z";
    private static final String REGISTER_T = "t";
    private static final String REGISTER_Y = "y";
    private static final String REGISTER_X_MANTISSA = "xMantissa";
    private static final String REGISTER_X_EXPONENT = "xExponent";
    private static final String RESET_X = "resetX";

    private static final String ENTRY_MODE = "entryMode";
    private static final String DECIMAL_FACTOR = "decimalFactor";
    private static final String CLASS_NAME = "net/ninjacat/mk54/Mk54";
    private static final String CLASS_DESCRIPTOR = "L" + CLASS_NAME + ";";

    public static final String JAVA_LANG_MATH = "java/lang/Math";
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
            .put(NEG, CodeGenerator::negateSign)
            .put(EXP, CodeGenerator::startExponent)
            .put(ENTER, CodeGenerator::enterNumber)
            .put(CX, CodeGenerator::clearX)
            .put(RESTORE_X, CodeGenerator::restoreX)
            .put(ADD, CodeGenerator::add)
            .put(SUB, CodeGenerator::sub)
            .put(MUL, CodeGenerator::mul)
            .put(DIV, CodeGenerator::div)
            .put(SWAP, CodeGenerator::swapXy)
            .put(TEN_TO_POWER_X, CodeGenerator::tenToPowerX)
            .put(E_TO_POWER_X, CodeGenerator::eToPowerX)
            .put(LOG10, CodeGenerator::log)
            .put(LN, CodeGenerator::ln)
            .put(ARCSIN, generateArcTrig("asin"))
            .put(ARCCOS, generateArcTrig("acos"))
            .put(ARCTAN, generateArcTrig("atan"))
            .put(SIN, generateTrig("sin"))
            .put(COS, generateTrig("cos"))
            .put(TAN, generateTrig("tan"))
            .build();


    CodeGenerator() {
        super();
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

        prepareXForReset(mv, context);
    }

    /**
     * Generates a function which creates a code to add a digit to register X
     *
     * @param digit digit to add
     * @return {@link OperationCodeGenerator} which generates digit-adding code
     * <p>
     */
    private static OperationCodeGenerator digit(final int digit) {
        return (mv, context) -> {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, RESET_X, "Z");
            final Label noReset = new Label();
            mv.visitJumpInsn(IFEQ, noReset);

            // Clear X if resetX flag is set
            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(FCONST_0);
            mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X_MANTISSA, "F");
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
    private static void negateSign(final MethodVisitor mv, final CodeGenContext context) {
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
    private static void restoreX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X1, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
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

    /**
     * Moves stack down from T to Z and Z  to Y. This must be executed for every operation which
     * combines Y and X and puts result to X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void stackDown(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Z, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Y, "F");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_T, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Z, "F");
    }

    /**
     * Saves X to X1
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void saveX(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X1, "F");
    }

    /**
     * Adds X to Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void add(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FADD);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
        prepareXForReset(mv, context);
    }

    /**
     * Multiplies X by Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void mul(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FMUL);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
        prepareXForReset(mv, context);
    }

    /**
     * Divides Y by X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void div(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FDIV);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
        prepareXForReset(mv, context);
    }

    /**
     * Subtracts X from Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void sub(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(FSUB);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        stackDown(mv, context);
        prepareXForReset(mv, context);
    }

    /**
     * Swaps X and Y
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void swapXy(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_Y, "F");
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        prepareXForReset(mv, context);
    }

    /**
     * Calculates ten to power of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void tenToPowerX(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(10.0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "pow", "(DD)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        prepareXForReset(mv, context);
    }

    /**
     * Calculates e to power of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void eToPowerX(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(Math.E);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "pow", "(DD)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        prepareXForReset(mv, context);
    }

    /**
     * Calculates log base 10 of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void log(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "log10", "(D)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        prepareXForReset(mv, context);
    }

    /**
     * Calculates natural logarithm of number in X
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void ln(final MethodVisitor mv, final CodeGenContext context) {
        saveX(mv, context);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
        mv.visitInsn(F2D);
        mv.visitMethodInsn(INVOKESTATIC, JAVA_LANG_MATH, "log", "(D)D", false);
        mv.visitInsn(D2F);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, REGISTER_X, "F");
        prepareXForReset(mv, context);
    }

    /**
     * Generates code for call of single-argument trigonometry function from Math class (sin/cos/tan)
     *
     * @param function Name of the function
     * @return {@link OperationCodeGenerator}
     * <p>
     * TODO: Use rad-grad-deg switch
     */
    private static OperationCodeGenerator generateTrig(final String function) {
        return (mv, context) -> {
            saveX(mv, context);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/ninjacat/mk54/Mk54", "radGradDeg", "I");

            final Label rad = new Label();
            final Label grad = new Label();
            final Label deg = new Label();
            final Label defaultBlock = new Label();

            mv.visitTableSwitchInsn(0, 2, defaultBlock, rad, grad, deg);

            // radians
            mv.visitLabel(rad);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBlock);

            // degrees
            mv.visitLabel(deg);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, "toRadians", "(D)D", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBlock);

            // grads
            mv.visitLabel(grad);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitLdcInsn(Math.PI);
            mv.visitInsn(Opcodes.DMUL);
            mv.visitLdcInsn(200.0);
            mv.visitInsn(Opcodes.DDIV);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

            mv.visitLabel(defaultBlock);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

            prepareXForReset(mv, context);
        };
    }

    private static OperationCodeGenerator generateArcTrig(final String function) {
        return (mv, context) -> {
            saveX(mv, context);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, "net/ninjacat/mk54/Mk54", "radGradDeg", "I");
            final Label rad = new Label();
            final Label deg = new Label();
            final Label grad = new Label();
            final Label defaultBranch = new Label();
            mv.visitTableSwitchInsn(0, 2, defaultBranch, rad, grad, deg);

            mv.visitLabel(rad);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBranch);

            mv.visitLabel(deg);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, "toDegrees", "(D)D", false);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitJumpInsn(Opcodes.GOTO, defaultBranch);

            mv.visitLabel(grad);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitLdcInsn(200.0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_NAME, REGISTER_X, "F");
            mv.visitInsn(Opcodes.F2D);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, JAVA_LANG_MATH, function, "(D)D", false);
            mv.visitInsn(Opcodes.DMUL);
            mv.visitLdcInsn(Math.PI);
            mv.visitInsn(Opcodes.DDIV);
            mv.visitInsn(Opcodes.D2F);
            mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, REGISTER_X, "F");

            mv.visitLabel(defaultBranch);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

            prepareXForReset(mv, context);
        };
    }

    /**
     * Helper method called on all operations. Sets resetX flag to true
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    private static void prepareXForReset(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, CLASS_NAME, RESET_X, "Z");
    }

    byte[] compile(final String operationsStr) {
        final List<String> operations = ImmutableList.copyOf(
                Splitter.onPattern("\\s+").omitEmptyStrings().trimResults().split(operationsStr));

        // Set up ASM
        final ClassReader reader;
        try {
            reader = new ClassReader(Mk54.class.getName());
        } catch (final Exception ex) {
            throw new ClassCreationException("Failed to create class", ex);
        }
        final ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);

        final ClassVisitor cv = new ClassVisitor(ASM6, classWriter) {
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
