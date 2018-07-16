package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.Mk54;
import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CalculationTests {


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
    public void shouldCalculateTenToPowerOfX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DECIMAL_POINT,
                DIGIT_3,
                TEN_TO_POWER_X
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(19.952621f));
    }

    @Test
    public void shouldCalculateEToPowerOfX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DECIMAL_POINT,
                DIGIT_5,
                E_TO_POWER_X
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(4.481689f));
    }

    @Test
    public void shouldCalculateLog10() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_1,
                DIGIT_0,
                DIGIT_0,
                DIGIT_0,
                LOG10
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(3f));
    }

    @Test
    public void shouldCalculateLog() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_2,
                DIGIT_0,
                LN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(2.9957323f));
    }

    @Test
    public void shouldCalculateSinInRadians() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_0,
                DECIMAL_POINT,
                DIGIT_5,
                SIN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.47942555f));
    }

    @Test
    public void shouldCalculateSinInDegrees() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_4,
                DIGIT_0,
                SIN
        ));

        mk54.setRadGradDeg(Mk54.DEG);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.64278764f));
    }

    @Test
    public void shouldCalculateSinInGrads() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                DIGIT_0,
                SIN
        ));

        mk54.setRadGradDeg(Mk54.GRAD);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.70710678118f));
    }

    @Test
    public void shouldCalculateASinInRadians() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_0,
                DECIMAL_POINT,
                DIGIT_4,
                DIGIT_7,
                DIGIT_9,
                DIGIT_4,
                DIGIT_2,
                DIGIT_5,
                DIGIT_5,
                DIGIT_5,
                ARCSIN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(0.5, 0.00001));
    }

    @Test
    public void shouldCalculateASinInDegrees() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_0,
                DECIMAL_POINT,
                DIGIT_6,
                DIGIT_4,
                DIGIT_2,
                DIGIT_7,
                DIGIT_8,
                DIGIT_7,
                DIGIT_6,
                DIGIT_4,
                ARCSIN
        ));

        mk54.setRadGradDeg(Mk54.DEG);
        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(40, 0.00001));
    }

    @Test
    public void shouldCalculateASinInGrads() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_0,
                DECIMAL_POINT,
                DIGIT_7,
                DIGIT_0,
                DIGIT_7,
                DIGIT_1,
                DIGIT_0,
                DIGIT_6,
                DIGIT_7,
                DIGIT_8,
                ARCSIN
        ));

        mk54.setRadGradDeg(Mk54.GRAD);
        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(50, 0.00001));
    }

    @Test
    public void shouldPutPiIntoX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_0,
                DECIMAL_POINT,
                DIGIT_7,
                DIGIT_0,
                DIGIT_7,
                PI
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(Math.PI, 0.0000001)));

    }

    @Test
    public void shouldCalculateSquareRoot() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_2,
                DIGIT_5,
                SQRT
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(5f, 0.0000001)));
    }

    @Test
    public void shouldCalculatePow2() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_5,
                POW2
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(25f, 0.0000001)));
    }

    @Test
    public void shouldCalculateInv() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_2,
                INV
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(0.5f, 0.0000001)));
    }

    @Test
    public void shouldCalculateXPowY() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                DIGIT_2,
                ENTER,
                DIGIT_5,
                X_POW_Y
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(25f, 0.0000001)));

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

    @Test
    public void shouldCalculateAbsoluteValue() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                ABS
        ));

        mk54.setX(-5);
        mk54.execute();
        assertThat(mk54.getX(), is(5f));

        mk54.setX(12);
        mk54.execute();
        assertThat(mk54.getX(), is(12f));

    }


    @Test
    public void shouldDetermineSignOfX() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                SIGN
        ));

        mk54.setX(-5);
        mk54.execute();
        assertThat(mk54.getX(), is(-1f));

        mk54.setX(12);
        mk54.execute();
        assertThat(mk54.getX(), is(1f));

        mk54.setX(0);
        mk54.execute();
        assertThat(mk54.getX(), is(0f));
    }

}
