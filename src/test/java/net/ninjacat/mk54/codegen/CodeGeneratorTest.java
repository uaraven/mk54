package net.ninjacat.mk54.codegen;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CodeGeneratorTest {

    private static Object getCompiledInstance(final String operations) throws Exception {
        final CodeGenerator codeGenerator = new CodeGenerator();
        final byte[] classBytes = codeGenerator.compile(operations);

        // TODO: remove class writing out
        try (final OutputStream os = new FileOutputStream("/tmp/Mk54.class")) {
            os.write(classBytes);
        }

        final Class mk54 = new ByteArrayClassLoader(classBytes).findClass("Mk54");

        return mk54.newInstance();
    }

    private static void execute(final Object mk54) throws Exception {
        final Method executeMethod = mk54.getClass().getMethod("execute");
        executeMethod.invoke(mk54);
    }

    private static float getX(final Object mk54) throws Exception {
        final Field x = mk54.getClass().getDeclaredField("x");
        return x.getFloat(mk54);
    }

    @Test
    public void shouldGenerateSimplestProgram() throws Exception {
        final Object mk54 = getCompiledInstance("01 02 03");

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(123));
    }
}