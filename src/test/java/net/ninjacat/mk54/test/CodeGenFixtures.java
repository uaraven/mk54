package net.ninjacat.mk54.test;

import net.ninjacat.mk54.codegen.CodeGenerator;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CodeGenFixtures {
    private CodeGenFixtures() {
    }

    public static Mk54Wrapper getCompiledInstance(final String operations) throws Exception {
        final CodeGenerator codeGenerator = new CodeGenerator(true);
        final byte[] classBytes = codeGenerator.compile(operations);

        final Path mk54Tmp = Paths.get(System.getProperty("java.io.tmpdir"), "Mk54.class");

        // TODO: remove class writing out
        try (final OutputStream os = new FileOutputStream(mk54Tmp.toFile())) {
            os.write(classBytes);
        }

        final Class mk54 = new ByteArrayClassLoader(classBytes).findClass("net.ninjacat.mk54.Mk54");

        return new Mk54Wrapper(mk54.newInstance());
    }

    public static String program(final String... operand) {
        return String.join(" ", operand);
    }
}
