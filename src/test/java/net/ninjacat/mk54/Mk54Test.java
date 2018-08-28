package net.ninjacat.mk54;

import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Mk54Test {

    @Test
    public void shouldAddDigitsToExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0, 0);

        mk54.exponentDigitEntry(1);
        assertThat(mk54.getX(), is(1.0e1));

        mk54.exponentDigitEntry(2);
        assertThat(mk54.getX(), is(closeTo(1.0e12, 1e-5)));
    }

    @Test
    public void shouldRollDigitsInExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0, 12);
        mk54.exponentDigitEntry(3);
        assertThat(mk54.getX(), closeTo(1.0e23, 1e8));
    }

    @Test
    public void shouldChangeSignOfExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0, 23);
        mk54.negateExponent();
        assertThat(mk54.getX(), is(1.0e-23));
    }

    @Test
    public void shouldChangeSignOfRegisterX() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0, 23);
        mk54.negateMantissa();
        assertThat(mk54.getX(), closeTo(-1.0e23, 1e8));
    }

    @Test
    public void shouldConvertMinDegSec2Deg() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(20.3648, 0);
        mk54.degMinSecToDegree();
        assertThat(mk54.getX(), closeTo(20.613332, 1e-5));
    }

    @Test
    public void shouldConvertDeg2DegMinSec() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(20.613332, 0);
        mk54.degreeToDegMinSec();
        assertThat(mk54.getX(), closeTo(20.3648, 1e-5));
    }
}