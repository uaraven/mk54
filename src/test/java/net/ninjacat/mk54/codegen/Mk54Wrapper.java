package net.ninjacat.mk54.codegen;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class Mk54Wrapper {

    private Object mk54;

    Mk54Wrapper(final Object mk54) {
        this.mk54 = mk54;
    }

    void execute() throws Exception {
        final Method executeMethod = this.mk54.getClass().getMethod("execute");
        executeMethod.invoke(this.mk54);
    }

    float getX() throws Exception {
        return getRegister("x");
    }

    private float getRegister(final String register) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        return x.getFloat(this.mk54);
    }

    float getX1() throws Exception {
        return getRegister("x1");
    }

    void setX1(final float value) throws Exception {
        setRegister("x1", value);
    }

    float getY() throws Exception {
        return getRegister("y");
    }

    void setX(final float value) throws Exception {
        setRegister("x", value);
    }

    float getZ() throws Exception {
        return getRegister("z");
    }

    private void setRegister(final String register, final float value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        x.setFloat(this.mk54, value);
    }

    float getT() throws Exception {
        return getRegister("t");
    }

    void setY(final float value) throws Exception {
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

    boolean getResetX() throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("resetX");
        x.setAccessible(true);
        return x.getBoolean(this.mk54);
    }

    void setResetX(final boolean value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField("resetX");
        x.setAccessible(true);
        x.setBoolean(this.mk54, value);
    }


    void setZ(final float value) throws Exception {
        setRegister("z", value);
    }

    void setT(final float value) throws Exception {
        setRegister("t", value);
    }


    float getMem(final int location) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        return Array.getFloat(mem.get(this.mk54), location);
    }

    void setMem(final int location, final float value) throws Exception {
        final Field mem = this.mk54.getClass().getDeclaredField("memory");
        mem.setAccessible(true);
        Array.setFloat(mem.get(this.mk54), location, value);
    }
}
