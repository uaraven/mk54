package net.ninjacat.mk54.codegen;


import org.objectweb.asm.MethodVisitor;

/**
 * Function responsible for generating code for one MK operation
 */
@FunctionalInterface
public interface OperationCodeGenerator {
    void generate(MethodVisitor mv, CodeGenContext context);
}
