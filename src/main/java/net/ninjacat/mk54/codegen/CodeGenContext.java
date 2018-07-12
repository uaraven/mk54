package net.ninjacat.mk54.codegen;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.ListIterator;

public class CodeGenContext {

    private final List<String> instructions;
    private final ListIterator<String> instructionPointer;

    private final ClassVisitor classVisitor;
    private final MethodVisitor methodVisitor;

    public CodeGenContext(final List<String> instructions, final ClassVisitor classVisitor, final MethodVisitor methodVisitor) {
        this.instructions = instructions;
        this.classVisitor = classVisitor;
        this.methodVisitor = methodVisitor;
        this.instructionPointer = this.instructions.listIterator();
    }

    public List<String> getInstructions() {
        return this.instructions;
    }

    public ListIterator<String> getInstructionPointer() {
        return this.instructionPointer;
    }

    public ClassVisitor getClassVisitor() {
        return this.classVisitor;
    }

    public MethodVisitor getMethodVisitor() {
        return this.methodVisitor;
    }
}
