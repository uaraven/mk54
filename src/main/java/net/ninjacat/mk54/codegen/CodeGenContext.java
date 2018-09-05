package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.Mk54;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static net.ninjacat.mk54.codegen.CodeGenUtil.CLASS_NAME;
import static net.ninjacat.mk54.opcodes.Opcode.shouldResetX;
import static org.objectweb.asm.Opcodes.*;

/**
 * Code generation context
 * <p>
 * This class contains all the data required for code generation, such as list of MK operations, ASM {@link MethodVisitor}
 * for generated method, list of all labels corresponding to MK operations, etc.
 */
class CodeGenContext {

    private final List<String> operations;

    private final Label trampolineLabel;
    private final Map<Integer, Label> addressLabels;
    private final boolean generateDebugCode;
    private int address;

    /**
     * Creates code generation context
     *
     * @param operations        List of MK operations
     * @param generateDebugCode
     */
    CodeGenContext(final List<String> operations, final boolean generateDebugCode) {
        this.operations = operations;
        this.trampolineLabel = new Label();
        this.addressLabels = new HashMap<>();
        this.address = 0;
        this.generateDebugCode = generateDebugCode;
    }

    List<String> getOperations() {
        return this.operations;
    }

    /**
     * Gets current operation address
     * <p>
     * Current address is set to 0 at the beginning of code generation and increase by 1 (or 2) with each processed
     * operation
     *
     * @return Current operation address
     */
    int getCurrentAddress() {
        return this.address;
    }

    /**
     * Advances current address and returns operation at this address
     *
     * @return Operation code
     */
    String nextOperation() {
        this.address += 1;
        return currentOperation();
    }

    /**
     * Gets operation at current address
     *
     * @return Operation code
     */
    private String currentOperation() {
        if (this.operations.size() > this.address) {
            return this.operations.get(this.address);
        } else {
            return null;
        }
    }

    /**
     * Returns a label for a give address.
     * If label for address does not exist it will create new and return it.
     * If label for address already exists it will return existing label
     *
     * @param addressOffset address for which label is requested
     * @return Label for the given address
     */
    Label getLabelForAddress(final int addressOffset) {
        if (this.addressLabels.containsKey(addressOffset)) {
            return this.addressLabels.get(addressOffset);
        } else {
            final Label label = new Label();
            this.addressLabels.put(addressOffset, label);
            return label;
        }
    }

    Label[] generateJumpTable() {
        return IntStream.range(0, this.operations.size())
                .mapToObj(this::getLabelForAddress)
                .toArray(Label[]::new);
    }

    Label getTrampolineLabel() {
        return this.trampolineLabel;
    }

    /**
     * Creates label for the second byte of two-byte MK operations.
     * <p>
     * This method creates a label for current address pointing to previous MK address
     *
     * <p>
     * For example following MK operation generates two bytes:
     * <pre>
     *   10. GOTO
     *   11. 65
     *   </pre>
     * There may be a jump somewhere in the code to a second byte of this command (address 11), but there is no
     * corresponding byte code.
     * <p>
     * This method must be called by code generators for such two byte commands to duplicate label of MK address 10
     * to MK address 11. This changes behaviour of calculator program, but there is no other option.
     */
    void duplicateLabelForTwoByteOperation() {
        this.addressLabels.put(getCurrentAddress(), getLabelForAddress(getCurrentAddress() - 1));
    }

    /**
     * Resets current address pointer to 0
     */
    void resetAddress() {
        this.address = 0;
    }

    /**
     * @return Code of current operation
     */
    String getCurrentOperation() {
        return this.operations.get(this.address);
    }

    public boolean isGenerateDebugCode() {
        return this.generateDebugCode;
    }


    /**
     * Generates call to {@link Mk54#debug(int, String)} which prints current address, operation code and state of
     * registers and memory
     *
     * @param mv {@link MethodVisitor} for {@code Mk54.execute()} method
     */
    public void generateDump(final MethodVisitor mv) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, this.getCurrentAddress());
        if (this.getCurrentAddress() >= this.operations.size()) {
            mv.visitLdcInsn("POST-OP");
        } else {
            mv.visitLdcInsn(this.getCurrentOperation());
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "debug", "(ILjava/lang/String;)V", false);
    }

    public void generatePostOp(final MethodVisitor mv) {
        final String op = getCurrentOperation();
        if (shouldResetX(op)) {
            CodeGenUtil.switchToCalculationMode(mv);
            CodeGenUtil.prepareXForReset(mv);
        } else {
            CodeGenUtil.doNotResetX(mv);
        }
    }
}
