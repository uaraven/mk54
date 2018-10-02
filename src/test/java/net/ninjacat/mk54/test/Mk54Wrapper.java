package net.ninjacat.mk54.test;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Mk54Wrapper {

    private final Object mk54;

    public Mk54Wrapper(final Object mk54) {
        this.mk54 = mk54;
    }

    public void execute() throws Exception {
        final Method executeMethod = this.mk54.getClass().getMethod("execute");
        executeMethod.invoke(this.mk54);
    }

    public double getX() throws Exception {
        return getRegister("x");
    }

    private double getRegister(final String register) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        return x.getDouble(this.mk54);
    }

    public double getX1() throws Exception {
        return getRegister("x1");
    }

    public void setX1(final double value) throws Exception {
        setRegister("x1", value);
    }

    public double getY() throws Exception {
        return getRegister("y");
    }

    public void setX(final double value) throws Exception {
        setRegister("x", value);
    }

    public double getZ() throws Exception {
        return getRegister("z");
    }

    private void setRegister(final String register, final double value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        x.setDouble(this.mk54, value);
    }

    public double getT() throws Exception {
        return getRegister("t");
    }

    public void setY(final double value) throws Exception {
        setRegister("y", value);
    }

    public int getRadGradDeg() throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("radGradDeg");
        x.setAccessible(true);
        return x.getInt(this.mk54);
    }

    public void setRadGradDeg(final int value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("radGradDeg");
        x.setAccessible(true);
        x.setInt(this.mk54, value);
    }

    public boolean getResetX() throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("resetX");
        x.setAccessible(true);
        return x.getBoolean(this.mk54);
    }

    public void setResetX(final boolean value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("resetX");
        x.setAccessible(true);
        x.setBoolean(this.mk54, value);
    }


    public void setZ(final double value) throws Exception {
        setRegister("z", value);
    }

    public void setT(final double value) throws Exception {
        setRegister("t", value);
    }


    public double getMem(final int location) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        return Array.getDouble(mem.get(this.mk54), location);
    }

    public void setMem(final int location, final double value) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        Array.setDouble(mem.get(this.mk54), location, value);
    }

    public void setStartAddress(final int address) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("startingAddress");
        mem.setAccessible(true);
        mem.setInt(this.mk54, address);
    }

}
