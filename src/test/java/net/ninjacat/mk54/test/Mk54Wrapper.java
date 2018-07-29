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

    public float getX() throws Exception {
        return getRegister("x");
    }

    private float getRegister(final String register) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        return x.getFloat(this.mk54);
    }

    public float getX1() throws Exception {
        return getRegister("x1");
    }

    public void setX1(final float value) throws Exception {
        setRegister("x1", value);
    }

    public float getY() throws Exception {
        return getRegister("y");
    }

    public void setX(final float value) throws Exception {
        setRegister("x", value);
    }

    public float getZ() throws Exception {
        return getRegister("z");
    }

    private void setRegister(final String register, final float value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        x.setFloat(this.mk54, value);
    }

    public float getT() throws Exception {
        return getRegister("t");
    }

    public void setY(final float value) throws Exception {
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


    public void setZ(final float value) throws Exception {
        setRegister("z", value);
    }

    public void setT(final float value) throws Exception {
        setRegister("t", value);
    }


    public float getMem(final int location) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        return Array.getFloat(mem.get(this.mk54), location);
    }

    public void setMem(final int location, final float value) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        Array.setFloat(mem.get(this.mk54), location, value);
    }
}
