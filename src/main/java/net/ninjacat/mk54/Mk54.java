package net.ninjacat.mk54;

import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Stack;

/**
 * Template class for Mk54 runnable program
 * <p>
 * This class contains operations stack, memory registers and helper methods.
 * <p>
 * During bytecode generation new class will be created based on this class but with execute() method containing
 * actual bytecode generated from mk hex code.
 */
@SuppressWarnings({"WeakerAccess", "unused", "FieldMayBeFinal", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray"})
public class Mk54 {

    public static final int RAD = 0;
    public static final int GRAD = 1;
    public static final int DEG = 2;

    private static final int MANTISSA = 0;
    private static final int EXPONENT = 1;

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
    /**
     * rad-grad-deg switch
     */
    private int radGradDeg = RAD;

    /**
     * Helper variables for managing X register
     */
    private float xMantissa;
    private int xExponent;
    /**
     * If true, any new digit will first reset register X to zero
     */
    private boolean resetX;

    /**
     * MK address - target of indirect jump
     */
    private int indirectJumpAddress;

    /**
     * Call stack for subroutine calls
     */
    private Stack<Integer> callStack;

    /**
     * Holds last generated random value
     */
    private float lastRandom;


    public Mk54() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.t = 0;
        this.x1 = 0;
        this.memory = new float[15];
        Arrays.fill(this.memory, 0f);
        this.resetX = true;
        this.lastRandom = 0;
        this.callStack = new Stack<>();
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
     * Parses parameters, sets up initial state and calls generated execute() method.
     * <p>
     * After execution is complete will print values from the stack and, optionally, memory registers
     *
     * @param args Command line arguments
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    public static void main(final String[] args) throws Exception {
        final Mk54 mk54 = new Mk54();

        final Method execute = Mk54.class.getDeclaredMethod("execute");
        execute.invoke(mk54);
    }

    /**
     * Performs testAsm entry to exponent. Exponent in MK-series consists of only 2 digits and if third testAsm is entered
     * it will push the first one out. If exponent value is 56 and number 8 is entered then exponent will change to
     * 68
     *
     * @param digit new exponent testAsm
     */
    @VisibleForTesting
    void exponentDigitEntry(final int digit) {
        final int sign = this.xExponent >= 0 ? 1 : -1;
        this.xExponent = sign * (Math.abs(this.xExponent) % 10 * 10 + digit);
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
            this.xMantissa += (float) digit / this.decimalFactor;
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
        if (this.xExponent != 0 && this.xMantissa == 0) {
            this.xMantissa = 1;
        }
        this.x = (float) (this.xMantissa * Math.pow(10, this.xExponent));
    }

    /**
     * Helper for PRN generator. Gets digit in 6th position of the number
     * of X register
     *
     * @return Digit in the 6th position of the X register or 0
     */
    private float getSegment() {
        final String registerString = new StringBuilder().append(this.x).reverse().toString().replaceAll("\\.", "");
        if (registerString.length() >= 6) {
            return Float.valueOf(registerString.substring(6, 7));
        } else {
            return 0f;
        }
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

    /**
     * Test method for getting asmified code
     */
    private void testAsm() {
        if (x >= 0) {
            indirectJumpAddress = 0;
        } else {
            indirectJumpAddress = 1;
        }
    }

}
