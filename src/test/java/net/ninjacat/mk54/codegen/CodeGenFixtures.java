package net.ninjacat.mk54.codegen;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

final class CodeGenFixtures {
    private CodeGenFixtures() {
    }

    static Mk54Wrapper getCompiledInstance(final String operations) throws Exception {
        final CodeGenerator codeGenerator = new CodeGenerator();
        final byte[] classBytes = codeGenerator.compile(operations);

        // TODO: remove class writing out
        try (final OutputStream os = new FileOutputStream("/tmp/Mk54.class")) {
            os.write(classBytes);
        }

        final Class mk54 = new ByteArrayClassLoader(classBytes).findClass("net.ninjacat.mk54.Mk54");

        return new Mk54Wrapper(mk54.newInstance());
    }

    static String program(final String... operand) {
        return Arrays.stream(operand).collect(Collectors.joining(" "));
    }
}
