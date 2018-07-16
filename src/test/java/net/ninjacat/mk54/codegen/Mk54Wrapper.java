package net.ninjacat.mk54.codegen;

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

    private void setRegister(final String register, final float value) throws Exception {
        final Field x = this.mk54.getClass().getDeclaredField(register);
        x.setAccessible(true);
        x.setFloat(this.mk54, value);
    }

    float getY() throws Exception {
        return getRegister("y");
    }

    float getZ() throws Exception {
        return getRegister("z");
    }

    float getT() throws Exception {
        return getRegister("t");
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

}
