package net.ninjacat.mk54;

import com.google.common.annotations.VisibleForTesting;

import java.util.stream.IntStream;

/**
 * Template class for Mk54 runnable program
 * <p>
 * This class contains operations stack, memory registers, helper methods and empty {@link #execute()} method.
 * During bytecode generation new class will be created based on this class but with execute() method containing
 * actual bytecode generated from mk hex code.
 */
public class Mk54 {

    private static final int MANTISSA = 0;
    private static final int EXPONENT = 1;
    private static final int EXPONENT_MASK = 0b01111111_10000000_00000000_00000000;
    private static final int MANTISSA_BITS = 23;
    private static final int EXPONENT_BIAS = 127;
    /**
     * Memory registers
     */
    private final float[] memory;
    /**
     * Switches between entering digits for mantissa or exponent
     */
    private int entryMode = MANTISSA;
    private int decimalFactor = 0;
    /**
     * Stack registers
     */
    private float x, y, z, t;
    private float xMantissa;
    private int xExponent;
    private float x1;

    Mk54() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.t = 0;
        this.memory = new float[14];
        this.x1 = 0;
        IntStream.range(0, this.memory.length).forEach(idx -> this.memory[idx] = 0);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public float getT() {
        return this.t;
    }

    public float getX1() {
        return this.x1;
    }

    /**
     * Main method for running this class from command line.
     * <p>
     * Parses parameters, sets up initial state and calls {@link #execute()} method.
     * <p>
     * After execution is complete will print values from the stack and, optionally, memory registers
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        final Mk54 mk54 = new Mk54();

        mk54.execute();
    }

    /**
     * Performs digit entry to exponent. Exponent in MK-series consists of only 2 digits and if third digit is entered
     * it will push the first one out. If exponent value is 56 and number 8 is entered then exponent will change to
     * 68
     *
     * @param digit new exponent digit
     */
    @VisibleForTesting
    void exponentDigitEntry(final int digit) {
        this.xExponent = Math.abs(this.xExponent) % 10 * 10 + digit;
        makeXRegister();
    }

    /**
     * Changes exponent sign.
     */
    @VisibleForTesting
    void negateExponent() {
        this.xExponent = -this.xExponent;
        makeXRegister();
    }

    void mantissaDigitEntry(final int digit) {
        if (this.decimalFactor == 0) {
            this.xMantissa = this.xMantissa * 10f + digit;
        } else {
            this.xMantissa += digit / this.decimalFactor;
            this.decimalFactor *= 10;
        }
        makeXRegister();
    }

    void negateMantissa() {
        this.xMantissa = -this.xMantissa;
        makeXRegister();
    }

    /**
     * This method is called after any modification of X register
     */
    private void makeXRegister() {
        this.x = (float) (this.xMantissa * Math.pow(10, this.xExponent));
    }

    /**
     * Test method for getting asmified code
     */
    private void digit() {
        this.xMantissa = 0;
        this.xExponent = 0;
        makeXRegister();
    }

    /**
     * Placeholder method which will be replaced with actual bytecode during compilation
     */
    private void execute() {
    }

    /**
     * Internal method to set register x
     *
     * @param mantissa Value of mantissa
     * @param exponent Exponent
     */
    void setX(final float mantissa, final int exponent) {
        this.xMantissa = mantissa;
        this.xExponent = exponent;
        makeXRegister();
    }
}
