package net.ninjacat.mk54;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Mk54Test {

    @Test
    public void shouldAddDigitsToExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0f, 0);

        mk54.exponentDigitEntry(1);
        assertThat(mk54.getX(), is(1.0e1f));

        mk54.exponentDigitEntry(2);
        assertThat(mk54.getX(), is(1.0e12f));
    }

    @Test
    public void shouldRollDigitsInExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0f, 12);
        mk54.exponentDigitEntry(3);
        assertThat(mk54.getX(), is(1.0e23f));
    }

    @Test
    public void shouldChangeSignOfExponent() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0f, 23);
        mk54.negateExponent();
        assertThat(mk54.getX(), is(1.0e-23f));
    }

    @Test
    public void shouldChangeSignOfRegisterX() {
        final Mk54 mk54 = new Mk54();
        mk54.setX(1.0f, 23);
        mk54.negateMantissa();
        assertThat(mk54.getX(), is(-1.0e23f));
    }
}