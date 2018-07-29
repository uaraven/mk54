package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.Mk54;
import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static net.ninjacat.mk54.test.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.test.CodeGenFixtures.program;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CalculationTests {


    @Test
    public void shouldAddTwoNumbersAndStoreOldXinX1() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                ENTER,
                DIGIT(5),
                DIGIT(8),
                ADD
        ));

        mk54.execute();
        final float x = mk54.getX1();

        assertThat(x, is(58f));
    }


    @Test
    public void shouldAddTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                ENTER,
                DIGIT(5),
                DIGIT(8),
                ADD
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(70f));
    }


    @Test
    public void shouldCycleStackDown() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                ENTER,
                DIGIT(2),
                ENTER,
                DIGIT(3),
                ENTER,
                DIGIT(4),
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
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                ENTER,
                DIGIT(6),
                MUL
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(30f));
    }

    @Test
    public void shouldDivideTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(0),
                ENTER,
                DIGIT(5),
                DIV
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(10f));
    }

    @Test
    public void shouldSubtractTwoNumbers() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(0),
                ENTER,
                DIGIT(5),
                DIGIT(5),
                SUB
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(-5f));
    }


    @Test
    public void shouldCalculateTenToPowerOfX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DECIMAL_POINT,
                DIGIT(3),
                TEN_TO_POWER_X
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(19.952621f));
    }

    @Test
    public void shouldCalculateEToPowerOfX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DECIMAL_POINT,
                DIGIT(5),
                E_TO_POWER_X
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(4.481689f));
    }

    @Test
    public void shouldCalculateLog10() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(0),
                DIGIT(0),
                DIGIT(0),
                LOG10
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(3f));
    }

    @Test
    public void shouldCalculateLog() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(2),
                DIGIT(0),
                LN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(2.9957323f));
    }

    @Test
    public void shouldCalculateSinInRadians() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                DECIMAL_POINT,
                DIGIT(5),
                SIN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.47942555f));
    }

    @Test
    public void shouldCalculateSinInDegrees() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                DIGIT(0),
                SIN
        ));

        mk54.setRadGradDeg(Mk54.DEG);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.64278764f));
    }

    @Test
    public void shouldCalculateSinInGrads() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(0),
                SIN
        ));

        mk54.setRadGradDeg(Mk54.GRAD);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(0.70710678118f));
    }

    @Test
    public void shouldCalculateASinInRadians() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                DECIMAL_POINT,
                DIGIT(4),
                DIGIT(7),
                DIGIT(9),
                DIGIT(4),
                DIGIT(2),
                DIGIT(5),
                DIGIT(5),
                DIGIT(5),
                ARCSIN
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(0.5, 0.00001));
    }

    @Test
    public void shouldCalculateASinInDegrees() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                DECIMAL_POINT,
                DIGIT(6),
                DIGIT(4),
                DIGIT(2),
                DIGIT(7),
                DIGIT(8),
                DIGIT(7),
                DIGIT(6),
                DIGIT(4),
                ARCSIN
        ));

        mk54.setRadGradDeg(Mk54.DEG);
        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(40, 0.00001));
    }

    @Test
    public void shouldCalculateASinInGrads() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                DECIMAL_POINT,
                DIGIT(7),
                DIGIT(0),
                DIGIT(7),
                DIGIT(1),
                DIGIT(0),
                DIGIT(6),
                DIGIT(7),
                DIGIT(8),
                ARCSIN
        ));

        mk54.setRadGradDeg(Mk54.GRAD);
        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, closeTo(50, 0.00001));
    }

    @Test
    public void shouldPutPiIntoX() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                DECIMAL_POINT,
                DIGIT(7),
                DIGIT(0),
                DIGIT(7),
                PI
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(Math.PI, 0.0000001)));

    }

    @Test
    public void shouldCalculateSquareRoot() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(2),
                DIGIT(5),
                SQRT
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(5f, 0.0000001)));
    }

    @Test
    public void shouldCalculatePow2() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                POW2
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(25f, 0.0000001)));
    }

    @Test
    public void shouldCalculateInv() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(2),
                INV
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(0.5f, 0.0000001)));
    }

    @Test
    public void shouldCalculateXPowY() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(2),
                ENTER,
                DIGIT(5),
                X_POW_Y
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat((double) x, is(closeTo(25f, 0.0000001)));

    }

    @Test
    public void shouldResetXAfterOperation() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                ENTER,
                DIGIT(6),
                ADD
        ));

        mk54.execute();
        final boolean resetX = mk54.getResetX();

        assertThat(resetX, is(true));
    }

    @Test
    public void shouldCalculateAbsoluteValue() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
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
        final Mk54Wrapper mk54 = getCompiledInstance(program(
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

    @Test
    public void shouldTruncateFractionalPart() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                TRUNC
        ));

        mk54.setX(6.432f);
        mk54.execute();
        assertThat(mk54.getX(), is(6f));

        mk54.setX(-6.432f);
        mk54.execute();
        assertThat(mk54.getX(), is(-6f));

    }

    @Test
    public void shouldRemoveIntegerPart() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                FRAC
        ));

        mk54.setX(6.432f);
        mk54.execute();
        assertThat((double) mk54.getX(), closeTo(0.432f, 0.00001f));

        mk54.setX(-6.432f);
        mk54.execute();
        assertThat((double) mk54.getX(), closeTo(-0.432f, 0.00001));

    }

    @Test
    public void shouldFindMax() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                MAX
        ));

        mk54.setX(6.432f);
        mk54.setY(-43);
        mk54.execute();
        assertThat((double) mk54.getX(), closeTo(6.432f, 0.00001f));

        mk54.setX(-6.432f);
        mk54.setY(3);
        mk54.execute();
        assertThat((double) mk54.getX(), closeTo(3f, 0.00001));

        mk54.setX(0);
        mk54.setY(3);
        mk54.execute();
        assertThat(mk54.getX(), is(0f));

        mk54.setX(4);
        mk54.setY(0);
        mk54.execute();
        assertThat(mk54.getX(), is(0f));
    }

    @Test
    public void shouldGenerateRandomNumber() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                RND
        ));

        mk54.execute();
        assertThat(mk54.getX(), is(0.404067f));

        mk54.execute();
        assertThat(mk54.getX(), is(0.750957F));
    }
}
