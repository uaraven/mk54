package net.ninjacat.mk54.test;

import net.ninjacat.mk54.codegen.CodeGenerator;

import java.io.FileOutputStream;
import java.io.OutputStream;

public final class CodeGenFixtures {
    private CodeGenFixtures() {
    }

    public static Mk54Wrapper getCompiledInstance(final String operations) throws Exception {
        final CodeGenerator codeGenerator = new CodeGenerator(true);
        final byte[] classBytes = codeGenerator.compile(operations);

        // TODO: remove class writing out
        try (final OutputStream os = new FileOutputStream("/tmp/Mk54.class")) {
            os.write(classBytes);
        }

        final Class mk54 = new ByteArrayClassLoader(classBytes).findClass("net.ninjacat.mk54.Mk54");

        return new Mk54Wrapper(mk54.newInstance());
    }

    public static String program(final String... operand) {
        return String.join(" ", operand);
    }
}
