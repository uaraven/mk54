package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.exceptions.InvalidJumpTargetException;
import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static net.ninjacat.mk54.test.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.test.CodeGenFixtures.program;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ControlGenTest {

    @Test
    public void shouldStopProgram() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                STOP,
                DIGIT(3),
                DIGIT(4)
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(12f));
    }

    @Test
    public void shouldGoToAddress() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                GOTO,
                "06",
                DIGIT(3),
                DIGIT(4),
                NOP
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(12f));
    }

    @Test
    public void gotoToSecondByteShouldJumpToInstructionStart() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                GOTO,
                "03",
                GOTO,
                "04",
                DIGIT(9)
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(9f));
    }

    @Test
    public void shouldGoToSubroutineAndReturn() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                GOSUB,
                "05",
                DIGIT(2),
                ADD,
                STOP,
                DIGIT(9),
                ENTER,
                RET
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(11f));
    }

    @Test
    public void shouldSupportNestedSubroutines() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                GOSUB,
                "06",
                DIGIT(2),
                ADD,
                ADD,
                STOP,
                DIGIT(9),
                ENTER,
                GOSUB,
                "11",
                RET,
                DIGIT(1),
                RET
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(12f));
    }

    @Test(expected = InvalidJumpTargetException.class)
    public void shouldFailJumpBeyonLastOperation() throws Exception {
        getCompiledInstance(program(
                GOTO,
                "44"
        ));
        fail("Should have failed during code generation");
    }

    @Test
    public void shouldCorrectlyHandleJNZ() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                JNZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        ));

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        float x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2f));

        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));
    }

    @Test
    public void shouldCorrectlyHandleJZ() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                JZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        ));

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        float x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));

        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2f));
    }

    @Test
    public void shouldCorrectlyHandleJLTZ() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                JLTZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        ));

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        float x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));

        mk54.setX(-5);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2f));

        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));

    }

    @Test
    public void shouldCorrectlyHandleJGEZ() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                JGEZ,
                "04",
                DIGIT(2),
                STOP,
                DIGIT(1),
                STOP
        ));

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        float x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));

        mk54.setX(-5);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2f));

        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1f));
    }

    @Test
    public void shouldDecreaseMemValueAndPerformIndirectJump() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                IGOTO(0),
                DIGIT(5),
                STOP
        ));

        mk54.setMem(0, 3);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(5f));
    }

    @Test
    public void shouldIncreaseMemValueAndPerformIndirectJump() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                IGOTO(5),
                DIGIT(5),
                STOP
        ));

        mk54.setMem(5, 2);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(4f));
    }

    @Test
    public void shouldPerformIndirectJump() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                IGOTO(10),
                STOP,
                DIGIT(5),
                STOP
        ));

        mk54.setMem(10, 3);
        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(5f));
    }

    @Test
    public void shouldExecuteLoop() throws Exception {
        testLoop(0);
        testLoop(1);
        testLoop(2);
        testLoop(3);
    }

    private static void testLoop(final int register) throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                STO(register),
                DIGIT(0),
                STO(5),
                RCL(5),
                DIGIT(1),
                ADD,
                STO(5),
                LOOP(register),
                "04",
                STOP
        ));

        mk54.execute();
        final float counter = mk54.getMem(register);
        final float accum = mk54.getMem(5);

        assertThat(counter, is(1f));
        assertThat(accum, is(4f));
    }
}