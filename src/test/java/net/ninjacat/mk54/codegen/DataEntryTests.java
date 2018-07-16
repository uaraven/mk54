package net.ninjacat.mk54.codegen;

import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataEntryTests {


    @Test
    public void shouldAddNumbersToMantissa() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(DIGIT_1, DIGIT_2, DIGIT_3));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(123f));
    }

    @Test
    public void shouldAddNumbersToMantissaWithDecimalPoint() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_2,
                DECIMAL_POINT,
                DIGIT_3,
                DIGIT_4
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(12.34f));
    }

    @Test
    public void shouldChangeSignOfRegisterX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_2,
                DECIMAL_POINT,
                DIGIT_3,
                DIGIT_4,
                NEG
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(-12.34f));
    }

    @Test
    public void shouldAddNumbersToExponent() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_3,
                EXP,
                DIGIT_2,
                DIGIT_5
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(13e25f));
    }

    @Test
    public void shouldChangeSignOfExponent() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_3,
                EXP,
                DIGIT_2,
                NEG,
                DIGIT_6
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(13e-26f));
    }


    @Test
    public void shouldResetRegisterXWhenEnterPressed() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_3,
                ENTER,
                DIGIT_5
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(5f));
    }

    @Test
    public void shouldClearResetFlagWhenInEntryMode() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_3,
                ENTER,
                DIGIT_5,
                ADD,
                DIGIT_9
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(9f));
        assertThat(mk54.getResetX(), is(false));
    }

    @Test
    public void shouldRestoreX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_3,
                ENTER,
                RESTORE_X
        ));

        mk54.setX1(42f);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(42f));
    }


    @Test
    public void shouldSwapXY() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                ENTER,
                DIGIT_3,
                SWAP
        ));

        mk54.execute();
        final float x = mk54.getX();
        final float y = mk54.getY();

        assertThat(x, is(1f));
        assertThat(y, is(3f));
    }

    @Test
    public void shouldRotateStack() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                ROT
        ));

        mk54.setX1(10);
        mk54.setX(1);
        mk54.setY(2);
        mk54.setZ(3);
        mk54.setT(4);
        mk54.execute();
        final float x = mk54.getX();
        final float y = mk54.getY();
        final float z = mk54.getZ();
        final float t = mk54.getT();
        final float x1 = mk54.getX1();

        assertThat(x, is(2f));
        assertThat(y, is(3f));
        assertThat(z, is(4f));
        assertThat(t, is(1f));
        assertThat(x1, is(1f));
    }
}
