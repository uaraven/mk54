package net.ninjacat.mk54.codegen;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Code generation context
 * <p>
 * This class contains all the data required for code generation, such as list of MK operations, ASM {@link MethodVisitor}
 * for generated method, list of all labels corresponding to MK operations, etc.
 */
public class CodeGenContext {

    private final List<String> operations;

    private final ClassVisitor classVisitor;
    private final MethodVisitor methodVisitor;
    private final Label trampolineLabel;
    private final Map<Integer, Label> addressLabels;
    private int address;

    /**
     * Creates code generation context
     *
     * @param operations    List of MK operations
     * @param classVisitor  {@link ClassVisitor} for class that is being generated
     * @param methodVisitor {@link MethodVisitor} for {@code execute()} method that is being generated
     */
    CodeGenContext(final List<String> operations,
                   final ClassVisitor classVisitor,
                   final MethodVisitor methodVisitor) {
        this.operations = operations;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
        this.trampolineLabel = new Label();
        this.addressLabels = new HashMap<>();
        this.address = 0;
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

    public ClassVisitor getClassVisitor() {
        return this.classVisitor;
    }

    public MethodVisitor getMethodVisitor() {
        return this.methodVisitor;
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

    public void incrementAddress() {
        this.address += 1;
    }

    Label[] generateJumpTable() {
        return IntStream.range(0, this.operations.size())
                .mapToObj(this::getLabelForAddress)
                .toArray(Label[]::new);
    }

    Label getTrampolineLabel() {
        return this.trampolineLabel;
    }
}
