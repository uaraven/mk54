package net.ninjacat.mk54;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Template class for Mk54 runnable program
 * <p>
 * This class contains operations stack, memory registers and helper methods.
 * <p>
 * During bytecode generation new class will be created based on this class but with execute() method containing
 * actual bytecode generated from mk hex code.
 */
@SuppressWarnings({"WeakerAccess", "unused", "FieldMayBeFinal", "FieldCanBeLocal", "MismatchedReadAndWriteOfArray", "SameParameterValue", "MagicNumber"})
public class Mk54 {

    public static final int RAD = 0;
    public static final int GRAD = 1;
    public static final int DEG = 2;

    private static final int MANTISSA = 0;
    private static final int EXPONENT = 1;

    /**
     * Memory registers
     */
    private final double[] memory;

    /**
     * Switches between entering digits for mantissa or exponent
     */
    private int entryMode = MANTISSA;
    private int decimalFactor = 0;


    /**
     * Stack registers
     */
    private double x, y, z, t, x1;
    /**
     * rad-grad-deg switch
     */
    private int radGradDeg = RAD;

    /**
     * Helper variables for managing X register
     */
    private double xMantissa;
    private int xExponent;
    /**
     * If true, any new digit will first reset register X to zero
     */
    private boolean resetX;
    /**
     * If true any digit entry will first push stack.
     * This is to distinguish between digit entry after other operation result and after other digit or В↑ operation
     * <p>
     * Stack should be pushed in the former case but not in the latter
     */
    private boolean pushStack;

    /**
     * MK address - target of indirect jump
     */
    private int indirectJumpAddress;

    /**
     * Call stack for subroutine calls
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Stack<Integer> callStack;

    /**
     * Holds last generated random value
     */
    private double lastRandom;
    /**
     * Address in MK address space from which program execution will start
     */
    private int startingAddress;


    public Mk54() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.t = 0;
        this.x1 = 0;
        this.memory = new double[15];
        Arrays.fill(this.memory, 0f);
        this.resetX = false;
        this.pushStack = false;
        this.lastRandom = 0;
        this.callStack = new Stack<>();
        this.startingAddress = 0;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public double getT() {
        return this.t;
    }

    public double getX1() {
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

        try {
            if (!parseArguments(args, mk54)) {
                return;
            }
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            help();
            return;
        }

        final Method execute = Mk54.class.getDeclaredMethod("execute");
        execute.invoke(mk54);

        mk54.dumpRegisters();
    }

    private static boolean parseArguments(final String[] args, final Mk54 mk54) {
        if (Arrays.stream(args).anyMatch("-h"::equalsIgnoreCase)) {
            help();
            return false;
        }
        final Iterator<String> argsIter = Arrays.stream(args).iterator();
        while (argsIter.hasNext()) {
            final String option = argsIter.next();
            mk54.setRegister(option.substring(1).toLowerCase(), Double.parseDouble(argsIter.next()));
        }
        return true;
    }

    /**
     * Prints help
     */
    private static void help() {
        System.out.println("Usage:");
        System.out.println("  java -jar mk54.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" -x VALUE    - sets value of X register");
        System.out.println(" -y VALUE    - sets value of Y register");
        System.out.println(" -z VALUE    - sets value of Z register");
        System.out.println(" -t VALUE    - sets value of T register");
        System.out.println(" -x1 VALUE   - sets value of X1 register");
        System.out.println(" -M<r> VALUE - sets value of memory address <r>, where <r> is number from 0 to E");
        System.out.println(" -s ADDRESS  - sets starting address for program execution");
        System.out.println(" -h          - prints this message");
    }

    private void setRegister(final String register, final double value) {
        switch (register) {
            case "x":
                this.x = value;
                break;
            case "x1":
                this.x1 = value;
                break;
            case "y":
                this.y = value;
                break;
            case "z":
                this.z = value;
                break;
            case "t":
                this.t = value;
                break;
            case "s":
                if (value < 0 || value > 104) {
                    throw new IllegalArgumentException("Invalid starting address, must be in 00..104 range");
                }
                this.startingAddress = (int) value;
                break;
            default:
                if (register.startsWith("m")) {
                    final int memReg = Integer.parseInt(register.substring(1));
                    this.memory[memReg] = value;
                }
        }
    }

    /**
     * Performs testAsm entry to exponent. Exponent in MK-series consists of only 2 digits and if third testAsm is entered
     * it will push the first one out. If exponent value is 56 and number 8 is entered then exponent will change to
     * 68
     *
     * @param digit new exponent testAsm
     */
    void exponentDigitEntry(final int digit) {
        final int sign = this.xExponent >= 0 ? 1 : -1;
        this.xExponent = sign * (Math.abs(this.xExponent) % 10 * 10 + digit);
        makeXRegister();
    }

    /**
     * Changes exponent sign.
     */
    void negateExponent() {
        this.xExponent = -this.xExponent;
        makeXRegister();
    }

    void mantissaDigitEntry(final int digit) {
        if (this.decimalFactor == 0) {
            this.xMantissa = this.xMantissa * 10f + digit;
        } else {
            this.xMantissa += (double) digit / this.decimalFactor;
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
        this.x = this.xMantissa * Math.pow(10, this.xExponent);
    }

    /**
     * Helper for PRN generator. Gets digit in 6th position of the number
     * of X register
     *
     * @return Digit in the 6th position of the X register or 0
     */
    private double getSegment() {
        final String registerString = new StringBuilder().append(this.x).reverse().toString().replaceAll("\\.", "");
        if (registerString.length() >= 6) {
            return Double.valueOf(registerString.substring(6, 7));
        } else {
            return 0f;
        }
    }

    void degMinSecToDegree() {
        final double degrees = (int) this.x;
        final double fp = frac(this.x);
        final double min = (int) (fp * 100);
        final double sec = (int) (frac(fp * 100) * 100);
        final double zzz = (int) (frac(fp * 10000) * 100);
        this.x = degrees + min / 60 + (sec + zzz / 100) / 3600;
    }

    void degreeToDegMinSec() {
        double pr = frac(this.x);
        final double min = (int) (pr * 60);
        pr -= min / 60;
        final double sec = (int) (pr * 3600);
        pr -= sec / 3600;
        final double zzz = (int) (pr * 360000);
        this.x = (int) this.x + min / 100 + sec / 10000 + zzz / 1000000;
    }

    void degMinToDegree() {
        final double degrees = (int) this.x;
        final double fp = frac(this.x);
        final double min = (int) (fp * 100);
        final double zzz = (int) (frac(fp * 1000) * 100);
        this.x = degrees + (min + zzz / 100) / 60;
    }

    void degreeToDegMin() {
        double pr = frac(this.x);
        final double min = (int) (pr * 60);
        pr -= min / 60;
        final double zzz = (int) (pr * 60000);
        this.x = (int) this.x + min / 100 + zzz / 100000;
    }

    private static double frac(final double d) {
        return d - (int) d;
    }

    /**
     * Internal method to set register x
     *
     * @param mantissa Value of mantissa
     * @param exponent Exponent
     */
    void setX(final double mantissa, final int exponent) {
        this.xMantissa = mantissa;
        this.xExponent = exponent;
        makeXRegister();
    }

    void debugPre(final int address, final String operation) {
        System.out.println(String.format("---- Pre op %s ----", operation));
        debug(address, operation);
        System.out.println("---------------------");
    }

    void debugPost(final int address, final String operation) {
        System.out.println(String.format("---- Post op %s ----", operation));
        debug(address, operation);
        System.out.println("======================");
    }

    private void debug(final int address, final String operation) {
        System.out.println(String.format("Addr: %X%X, Oper: %s", address / 10, address % 10, operation));
        System.out.println();
        dumpRegisters();
        System.out.println();
        dumpInternalState();
        System.out.println();
    }

    private void dumpInternalState() {
        System.out.println(String.format("      entryMode: %s", this.entryMode == MANTISSA ? "Mantissa" : "Exponent"));
        System.out.println(String.format("  decimalFactor: %d", this.decimalFactor));
        System.out.println(String.format("         resetX: %s", Boolean.toString(this.resetX)));
        System.out.println(String.format("      pushStack: %s", Boolean.toString(this.pushStack)));
        System.out.println(String.format("      Ret stack: [%s]", this.callStack.stream()
                .map(Integer::toHexString)
                .collect(Collectors.joining(" "))));
        System.out.println(String.format("indirectAddress: %s", this.indirectJumpAddress));
        System.out.println(String.format("startingAddress: %s", this.startingAddress));
    }

    void dumpRegisters() {
        System.out.println(String.format(" X: %f", this.x));
        System.out.println(String.format(" Y: %f", this.y));
        System.out.println(String.format(" Z: %f", this.z));
        System.out.println(String.format(" T: %f", this.t));
        System.out.println(String.format("X1: %f", this.x1));
        System.out.println();

        for (int i = 0; i < this.memory.length; i++) {
            System.out.print(String.format("M[%X]: %f", i, this.memory[i]));
            if ((i + 1) % 3 == 0) {
                System.out.println();
            } else {
                System.out.print("     ");
            }
        }
    }

    void asm() {
        degMinSecToDegree();
    }
}
