package net.ninjacat.mk54.codegen;

import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ArithmeticTests {


    @Test
    public void shouldAddTwoNumbersAndStoreOldXinX1() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_2,
                ENTER,
                DIGIT_5,
                DIGIT_8,
                ADD
        ));

        mk54.execute();
        final float x = mk54.getX1();

        assertThat(x, is(58f));
    }


    @Test
    public void shouldAddTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_2,
                ENTER,
                DIGIT_5,
                DIGIT_8,
                ADD
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(70f));
    }


    @Test
    public void shouldCycleStackDown() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                ENTER,
                DIGIT_2,
                ENTER,
                DIGIT_3,
                ENTER,
                DIGIT_4,
                ADD
        ));

        mk54.execute();
        final float t = mk54.getT();
        final float z = mk54.getZ();
        final float y = mk54.getY();

        assertThat(t, is(1f));
        assertThat(z, is(1f));
        assertThat(y, is(2f));
    }

    @Test
    public void shouldMultiplyTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                ENTER,
                DIGIT_6,
                MUL
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(30f));
    }

    @Test
    public void shouldDivideTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                DIGIT_0,
                ENTER,
                DIGIT_5,
                DIV
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(10f));
    }

    @Test
    public void shouldSubtractTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                DIGIT_0,
                ENTER,
                DIGIT_5,
                DIGIT_5,
                SUB
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(-5f));
    }


    @Test
    public void shouldResetXAfterOperation() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                ENTER,
                DIGIT_6,
                ADD
        ));

        mk54.execute();
        final boolean resetX = mk54.getResetX();

        assertThat(resetX, is(true));
    }

}
