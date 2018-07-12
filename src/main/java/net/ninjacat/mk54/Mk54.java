package net.ninjacat.mk54;

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
    private static final int EXPONENT_MASK = 0x7f800000;
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
    private float x1;

    private Mk54() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.t = 0;
        this.memory = new float[14];
        this.x1 = 0;
        IntStream.range(0, this.memory.length).forEach(idx -> this.memory[idx] = 0);
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

    private void exponentDigitEntry(final int digit) {
        int xint = Float.floatToIntBits(this.x);
        final int exponent = (xint & EXPONENT_MASK) >> MANTISSA_BITS;
        final int exponentValue = exponent - EXPONENT_BIAS;

        int newExponentValue = Math.abs(exponent) % 10 * 10 + digit;
        if (exponentValue < 0) {
            newExponentValue = -newExponentValue;
        }
        newExponentValue += EXPONENT_BIAS;

        final int exponentBits = newExponentValue << MANTISSA_BITS;
        xint = (xint | ~EXPONENT_MASK) + exponentBits;

        this.x = Float.intBitsToFloat(xint);
    }

    /**
     * Test method for getting asmified code
     *
     * @param n
     */
    private void digit() {
        if (this.entryMode == MANTISSA) {
            if (this.decimalFactor == 0) {
                this.x = this.x * 10 + 4;
            } else {
                this.x += 43 / this.decimalFactor;
                this.decimalFactor /= 10;
            }
        } else {
            exponentDigitEntry(44);
        }

    }

    /**
     * Placeholder method which will be replaced with actual bytecode during compilation
     */
    private void execute() {
    }
}
