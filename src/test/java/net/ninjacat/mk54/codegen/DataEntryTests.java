package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static net.ninjacat.mk54.test.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.test.CodeGenFixtures.program;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DataEntryTests {


    @Test
    public void shouldAddNumbersToMantissa() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(DIGIT(1), DIGIT(2), DIGIT(3)));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(123.0));
    }

    @Test
    public void shouldAddNumbersToMantissaWithDecimalPoint() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                DECIMAL_POINT,
                DIGIT(3),
                DIGIT(4)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(12.34));
    }

    @Test
    public void shouldChangeSignOfRegisterX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                DECIMAL_POINT,
                DIGIT(3),
                DIGIT(4),
                NEG
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(-12.34));
    }

    @Test
    public void shouldChangeXto1WhenStartingExponent() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                EXP,
                DIGIT(2),
                DIGIT(5)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(1e25));
    }


    @Test
    public void shouldAddNumbersToExponent() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                EXP,
                DIGIT(2),
                DIGIT(5)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, closeTo(13e25, 1e11));
    }

    @Test
    public void shouldChangeSignOfExponent() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                EXP,
                DIGIT(2),
                NEG,
                DIGIT(6)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(13e-26));
    }


    @Test
    public void shouldResetRegisterXWhenEnterPressed() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                ENTER,
                DIGIT(5)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
    }

    @Test
    public void shouldClearResetFlagWhenInEntryMode() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                ENTER,
                DIGIT(5),
                ADD,
                DIGIT(9)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(9.0));
        assertThat(mk54.getResetX(), is(false));
    }

    @Test
    public void shouldRestoreX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                ENTER,
                RESTORE_X
        ));

        mk54.setX1(42.0);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(42.0));
    }

    @Test
    public void shouldClearX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(3),
                CX
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(0.0));
    }



    @Test
    public void shouldSwapXY() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                ENTER,
                DIGIT(3),
                SWAP
        ));

        mk54.execute();
        final double x = mk54.getX();
        final double y = mk54.getY();

        assertThat(x, is(1.0));
        assertThat(y, is(3.0));
    }

    @Test
    public void shouldRotateStack() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                ROT
        ));

        mk54.setX1(10);
        mk54.setX(1);
        mk54.setY(2);
        mk54.setZ(3);
        mk54.setT(4);
        mk54.execute();
        final double x = mk54.getX();
        final double y = mk54.getY();
        final double z = mk54.getZ();
        final double t = mk54.getT();
        final double x1 = mk54.getX1();

        assertThat(x, is(2.0));
        assertThat(y, is(3.0));
        assertThat(z, is(4.0));
        assertThat(t, is(1.0));
        assertThat(x1, is(1.0));
    }

    @Test
    public void shouldEnterMaxNumber() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                DIGIT(9),
                EXP,
                DIGIT(9),
                DIGIT(9)
        ));

        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(99999999e99));

    }
}
