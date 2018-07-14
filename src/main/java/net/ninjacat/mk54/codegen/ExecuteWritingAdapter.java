package net.ninjacat.mk54.codegen;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;

public class ExecuteWritingAdapter extends ClassVisitor {

    public ExecuteWritingAdapter(final int i, final ClassVisitor classVisitor) {
        super(i, classVisitor);
    }

//    @Override
//    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
//        cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, CLASS_NAME, null, "java/lang/Object", null);
//    }


    @Override
    public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
        return super.visitMethod(i, s, s1, s2, strings);
    }
}
