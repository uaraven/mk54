package net.ninjacat.mk54;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Template class for Mk54 runnable program
 * <p>
 * This class contains operations stack, memory registers, helper methods and empty {@link #execute()} method.
 * During bytecode generation new class will be created based on this class but with execute() method containing
 * actual bytecode generated from mk hex code.
 */
public class Mk54 {

    private final List<Float> stack;
    private final float[] memory;
    private float x1;

    private Mk54() {
        this.stack = new LinkedList<>();
        this.memory = new float[14];
        IntStream.range(0, 4).forEach(idx -> this.stack.add(0.0f));
        IntStream.range(0, this.memory.length).forEach(idx -> this.memory[idx] = 0);
        this.x1 = 0;
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
     * Placeholder method which will be replaced with actual bytecode during compilation
     */
    private void execute() {
    }
}
