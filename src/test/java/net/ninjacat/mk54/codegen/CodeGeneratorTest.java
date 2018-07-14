package net.ninjacat.mk54.codegen;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import static net.ninjacat.mk54.opcodes.Opcode.*;
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

        final Class mk54 = new ByteArrayClassLoader(classBytes).findClass("net.ninjacat.mk54.Mk54");

        return mk54.newInstance();
    }

    private static String program(final String... operand) {
        return Arrays.stream(operand).collect(Collectors.joining(" "));
    }

    private static void execute(final Object mk54) throws Exception {
        final Method executeMethod = mk54.getClass().getMethod("execute");
        executeMethod.invoke(mk54);
    }

    private static float getX(final Object mk54) throws Exception {
        final Field x = mk54.getClass().getDeclaredField("x");
        x.setAccessible(true);
        return x.getFloat(mk54);
    }

    @Test
    public void shouldAddNumbersToMantissa() throws Exception {
        final Object mk54 = getCompiledInstance(program(DIGIT_1, DIGIT_2, DIGIT_3));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(123f));
    }

    @Test
    public void shouldAddNumbersToMantissaWithDecimalPoint() throws Exception {
        final Object mk54 = getCompiledInstance(program(
                DIGIT_1,
                DIGIT_2,
                DECIMAL_POINT,
                DIGIT_3,
                DIGIT_4
        ));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(12.34f));
    }

    @Test
    public void shouldChangeSignOfRegisterX() throws Exception {
        final Object mk54 = getCompiledInstance(program(
                DIGIT_1,
                DIGIT_2,
                DECIMAL_POINT,
                DIGIT_3,
                DIGIT_4,
                NEG
        ));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(-12.34f));
    }

    @Test
    public void shouldAddNumbersToExponent() throws Exception {
        final Object mk54 = getCompiledInstance(program(
                DIGIT_1,
                DIGIT_3,
                EXP,
                DIGIT_2,
                DIGIT_5
        ));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(13e25f));
    }

    @Test
    public void shouldChangeSignOfExponent() throws Exception {
        final Object mk54 = getCompiledInstance(program(
                DIGIT_1,
                DIGIT_3,
                EXP,
                DIGIT_2,
                NEG,
                DIGIT_6
        ));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(13e-26f));
    }


    @Test
    public void shouldResetRegisterXWhenEnterPressed() throws Exception {
        final Object mk54 = getCompiledInstance(program(
                DIGIT_1,
                DIGIT_3,
                ENTER,
                DIGIT_5
        ));

        execute(mk54);
        final float x = getX(mk54);

        assertThat(x, is(5f));
    }
}