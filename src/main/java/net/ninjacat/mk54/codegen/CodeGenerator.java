package net.ninjacat.mk54.codegen;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.ninjacat.mk54.Mk54;
import net.ninjacat.mk54.exceptions.ClassCreationException;
import net.ninjacat.mk54.exceptions.InvalidJumpTargetException;
import net.ninjacat.mk54.exceptions.UnknownOperationException;
import net.ninjacat.mk54.opcodes.Opcode;
import org.objectweb.asm.*;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static net.ninjacat.mk54.codegen.CodeGenUtil.*;
import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generates Java byte code for MK program.
 */
public class CodeGenerator {

    private static final ImmutableMap.Builder<String, OperationCodeGenerator> OPERATIONS_BUILDER = ImmutableMap.builder();

    private static final int MEMORY_SIZE = 15;
    private static final String ILLEGAL_STATE_EXCEPTION = "java/lang/IllegalStateException";
    static final String JAVA_LANG_INTEGER = "java/lang/Integer";

    static {
        OPERATIONS_BUILDER
                .put(DECIMAL_POINT, RegisterGen::decimal)
                .put(NEG, RegisterGen::negateSign)
                .put(EXP, CodeGenUtil::startExponent)
                .put(ENTER, RegisterGen::enterNumber)
                .put(CX, RegisterGen::clearX)
                .put(RESTORE_X, RegisterGen::restoreX)
                .put(ADD, MathGen::add)
                .put(SUB, MathGen::sub)
                .put(MUL, MathGen::mul)
                .put(DIV, MathGen::div)
                .put(Opcode.SWAP, RegisterGen::swapXy)
                .put(TEN_TO_POWER_X, MathGen::tenToPowerX)
                .put(E_TO_POWER_X, MathGen::eToPowerX)
                .put(LOG10, MathGen::log)
                .put(LN, MathGen::ln)
                .put(ARCSIN, MathGen.generateArcTrig("asin"))
                .put(ARCCOS, MathGen.generateArcTrig("acos"))
                .put(ARCTAN, MathGen.generateArcTrig("atan"))
                .put(SIN, MathGen.generateTrig("sin"))
                .put(COS, MathGen.generateTrig("cos"))
                .put(TAN, MathGen.generateTrig("tan"))
                .put(PI, MathGen::pi)
                .put(SQRT, MathGen::sqrt)
                .put(POW2, MathGen::pow2)
                .put(INV, MathGen::inv)
                .put(X_POW_Y, MathGen::xPowY)
                .put(ROT, RegisterGen::rotate)
                .put(FAIL1, CodeGenerator::fail)
                .put(FAIL2, CodeGenerator::fail)
                .put(FAIL3, CodeGenerator::fail)
                .put(ABS, MathGen::abs)
                .put(SIGN, MathGen::sign)
                .put(TRUNC, MathGen::trunc)
                .put(FRAC, MathGen::frac)
                .put(MAX, MathGen::max)
                .put(RND, MathGen::rnd)
                .put(STOP, ControlGen::startStop)
                .put(Opcode.GOTO, ControlGen::gotoAddr)
                .put(GOSUB, ControlGen::gosub)
                .put(Opcode.RET, ControlGen::returnFromSub)
                .put(Opcode.NOP, ControlGen::nop)
                .put(JNZ, ControlGen::jnz)
                .put(JZ, ControlGen::jz)
                .put(JLTZ, ControlGen::jltz)
                .put(JGEZ, ControlGen::jgez)
                .put(LOOP0, ControlGen.loop(0))
                .put(LOOP1, ControlGen.loop(1))
                .put(LOOP2, ControlGen.loop(2))
                .put(LOOP3, ControlGen.loop(3));

        IntStream.range(0, 10)
                .forEach(digit -> OPERATIONS_BUILDER.put(DIGIT(digit), RegisterGen.digit(digit)));

        IntStream.rangeClosed(0, MEMORY_SIZE)
                .forEach(mem -> {
                    OPERATIONS_BUILDER.put(STO(mem), MemoryGen.storeToMemory(mem));
                    OPERATIONS_BUILDER.put(RCL(mem), MemoryGen.recallFromMemory(mem));
                    OPERATIONS_BUILDER.put(IGOTO(mem), ControlGen.indirectGoto(mem));
                    OPERATIONS_BUILDER.put(ICALL(mem), ControlGen.icall(mem));
                    OPERATIONS_BUILDER.put(ISTO(mem), MemoryGen.istore(mem));
                    OPERATIONS_BUILDER.put(IRCL(mem), MemoryGen.irecall(mem));
                    OPERATIONS_BUILDER.put(IJNZ(mem), ControlGen.ijnz(mem));
                    OPERATIONS_BUILDER.put(IJZ(mem), ControlGen.ijz(mem));
                    OPERATIONS_BUILDER.put(IJGEZ(mem), ControlGen.ijgez(mem));
                    OPERATIONS_BUILDER.put(IJLZ(mem), ControlGen.ijltz(mem));
                });

    }

    private static final Map<String, OperationCodeGenerator> OPERATION_CODEGEN = OPERATIONS_BUILDER.build();
    private final boolean generateDebugCode;

    /**
     * Creates new instance of CodeGenerator
     * <p>
     * Will generate code without debug output
     */
    public CodeGenerator() {
        this(false);
    }

    /**
     * Creates new instance of CodeGenerator
     *
     * @param generateDebugCode Flag that determines whether to generate register dumping code for each operation
     */
    public CodeGenerator(final boolean generateDebugCode) {
        super();
        this.generateDebugCode = generateDebugCode;
    }

    private void generateExecuteMethod(final List<String> operations, final ClassVisitor classWriter) {
        final MethodVisitor executeMethod = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "execute", "()V", null, null);
        executeMethod.visitCode();
        final Label startLabel = new Label();
        executeMethod.visitLabel(startLabel);

        // Prepare context
        final CodeGenContext context = new CodeGenContext(operations);

        buildMkLabels(context);

        String operation = operations.get(0);
        while (operation != null) {
            generateOperandAddressLabel(executeMethod, context);
            if (OPERATION_CODEGEN.containsKey(operation)) {
                if (this.generateDebugCode) {
                    generateDump(executeMethod, context);
                }
                if (shouldResetX(operation)) {
                    CodeGenUtil.prepareXForReset(executeMethod, context);
                }
                OPERATION_CODEGEN.get(operation).generate(executeMethod, context);
            } else {
                throw new UnknownOperationException(operation);
            }
            operation = context.nextOperation();
        }

        final Label finalLabel = generateOperandAddressLabel(executeMethod, context);
        executeMethod.visitLabel(finalLabel);
        executeMethod.visitFrame(F_SAME, 0, null, 0, null);
        executeMethod.visitInsn(Opcodes.RETURN);
        generateTrampolineTable(executeMethod, context);
        executeMethod.visitLocalVariable("this", CLASS_DESCRIPTOR, null, startLabel, finalLabel, 0);
        executeMethod.visitMaxs(0, 0);
        executeMethod.visitEnd();

        classWriter.visitEnd();
    }

    /**
     * Generates call to {@link Mk54#debug(int, String)} which prints current address, operation code and state of
     * registers and memory
     *
     * @param mv      {@link MethodVisitor} for {@code Mk54.execute()} method
     * @param context Code generation context
     */
    private static void generateDump(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, context.getCurrentAddress());
        mv.visitLdcInsn(context.getCurrentOperation());
        mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "debug", "(ILjava/lang/String;)V", false);
    }

    /**
     * Scans all operations and build MK address to label map. Builds correct lables for two-byte operations
     *
     * @param context Code generation context
     */
    private static void buildMkLabels(final CodeGenContext context) {
        String op = context.getOperations().get(0);
        while (op != null) {
            context.getLabelForAddress(context.getCurrentAddress());
            if (Opcode.isJump(op)) {
                final int targetAddress = parseAddress(context.nextOperation());
                if (targetAddress > context.getOperations().size() - 1) {
                    throw new InvalidJumpTargetException(Integer.toHexString(targetAddress));
                }
                context.duplicateLabelForTwoByteOperation();
            }
            op = context.nextOperation();
        }
        context.resetAddress();
    }

    /**
     * Generates trampoline table for indirect jumps and subroutine returns.
     * <p>
     * To jump to MK address push MK address on stack and execute goto to label returned to {@link CodeGenContext#getTrampolineLabel()}
     * If MK address is invalid, IllegalStateException will be thrown
     *
     * @param mv      {@link MethodVisitor} for {@code Mk54.execute()} method
     * @param context Code generation context
     */
    private static void generateTrampolineTable(final MethodVisitor mv, final CodeGenContext context) {
        final Label defaultLabel = new Label();
        mv.visitLabel(context.getTrampolineLabel());
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, CLASS_NAME, ControlGen.INDIRECT_JUMP_ADDRESS, "I");
        mv.visitTableSwitchInsn(0, context.getOperations().size() - 1, defaultLabel, context.generateJumpTable());
        mv.visitLabel(defaultLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitTypeInsn(NEW, ILLEGAL_STATE_EXCEPTION);
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Invalid jump operation");
        mv.visitMethodInsn(INVOKESPECIAL, ILLEGAL_STATE_EXCEPTION, "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(ATHROW);
    }


    /**
     * Generates a label for each program step. This allows easy goto/conditional jmp implementation
     *
     * @param mv      {@link MethodVisitor} for generated method
     * @param context Code generating context
     */
    private static Label generateOperandAddressLabel(final MethodVisitor mv, final CodeGenContext context) {
        final Label opLabel = context.getLabelForAddress(context.getCurrentAddress());
        mv.visitLabel(opLabel);
        mv.visitLineNumber(context.getCurrentAddress(), opLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        return opLabel;
    }

    /**
     * Throws runtime exception with message "Error"
     *
     * @param mv      Generated method visitor
     * @param context Code generation context
     */
    @SuppressWarnings("unused")
    private static void fail(final MethodVisitor mv, final CodeGenContext context) {
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Error");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitFrame(F_SAME, 0, null, 0, null);
    }

    /**
     * Compiles MK-series operations into Java byte code.
     *
     * @param operationsStr String of operation codes separated by whitespace
     * @return byte array containing code fo generated class
     */
    public byte[] compile(final String operationsStr) {
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
