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

import static net.ninjacat.mk54.codegen.CodeGenUtil.CLASS_DESCRIPTOR;
import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.objectweb.asm.Opcodes.ASM6;

/**
 * Generates Java byte code for MK program.
 */
class CodeGenerator {

    private static final Map<String, OperationCodeGenerator> OPERATION_CODEGEN = ImmutableMap.<String, OperationCodeGenerator>builder()
            .put(DIGIT_0, RegisterGen.digit(0))
            .put(DIGIT_1, RegisterGen.digit(1))
            .put(DIGIT_2, RegisterGen.digit(2))
            .put(DIGIT_3, RegisterGen.digit(3))
            .put(DIGIT_4, RegisterGen.digit(4))
            .put(DIGIT_5, RegisterGen.digit(5))
            .put(DIGIT_6, RegisterGen.digit(6))
            .put(DIGIT_7, RegisterGen.digit(7))
            .put(DIGIT_8, RegisterGen.digit(8))
            .put(DIGIT_9, RegisterGen.digit(9))
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
            .put(SWAP, RegisterGen::swapXy)
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
