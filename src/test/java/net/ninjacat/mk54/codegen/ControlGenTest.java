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
        final double x = mk54.getX();

        assertThat(x, is(12.0));
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
        final double x = mk54.getX();

        assertThat(x, is(12.0));
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
        final double x = mk54.getX();

        assertThat(x, is(9.0));
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
        final double x = mk54.getX();

        assertThat(x, is(11.0));
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
        final double x = mk54.getX();

        assertThat(x, is(12.0));
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
        Mk54Wrapper mk54 = getCompiledInstance(program(
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
        double x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2.0));

        mk54 = getCompiledInstance(program(
                JNZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        ));
        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));
    }

    @Test
    public void shouldCorrectlyHandleJZ() throws Exception {
        final String program = program(
                JZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        );
        Mk54Wrapper mk54 = getCompiledInstance(program);

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        double x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));

        mk54 = getCompiledInstance(program);
        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2.0));
    }

    @Test
    public void shouldCorrectlyHandleJLTZ() throws Exception {
        final String program = program(
                JLTZ,
                "04",
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        );
        Mk54Wrapper mk54 = getCompiledInstance(program);

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        double x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));

        mk54 = getCompiledInstance(program);
        mk54.setX(-5);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2.0));

        mk54 = getCompiledInstance(program);
        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));

    }

    @Test
    public void shouldCorrectlyHandleJGEZ() throws Exception {
        final String program = program(
                JGEZ,
                "04",
                DIGIT(2),
                STOP,
                DIGIT(1),
                STOP
        );
        Mk54Wrapper mk54 = getCompiledInstance(program);

        mk54.setX(5);
        mk54.setResetX(true);
        mk54.execute();
        double x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));

        mk54 = getCompiledInstance(program);
        mk54.setX(-5);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 2 into X", x, is(2.0));


        mk54 = getCompiledInstance(program);
        mk54.setX(0);
        mk54.setResetX(true);
        mk54.execute();
        x = mk54.getX();

        assertThat("Should put 1 into X", x, is(1.0));
    }

    @Test
    public void shouldDecreaseMemValueAndPerformIndirectJump() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                IGOTO(0),
                DIGIT(5),
                STOP
        ));

        mk54.setMem(0, 3.0);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
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
        final double x = mk54.getX();

        assertThat(x, is(4.0));
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

        mk54.setMem(10, 3.0);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
    }

    @Test
    public void shouldDecreaseMemValueAndPerformIndirectCall() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                ICALL(0),
                STOP,
                DIGIT(5),
                RET
        ));

        mk54.setMem(0, 4);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
    }

    @Test
    public void shouldIncreaseMemValueAndPerformIndirectCall() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                ICALL(5),
                STOP,
                DIGIT(5),
                RET
        ));

        mk54.setMem(5, 2);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
    }

    @Test
    public void shouldPerformIndirectCall() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                ICALL(10),
                STOP,
                DIGIT(5),
                RET
        ));

        mk54.setMem(10, 3);
        mk54.execute();
        final double x = mk54.getX();

        assertThat(x, is(5.0));
    }

    @Test
    public void shouldPerformIndirectJNZ() throws Exception {
        testIndirectConditionalJump(IJNZ(0), 0, 4, 0, 0.1);
        testIndirectConditionalJump(IJNZ(5), 5, 2, 0, 0.1);
        testIndirectConditionalJump(IJNZ(10), 10, 3, 0, 0.1);
    }

    @Test
    public void shouldPerformIndirectJZ() throws Exception {
        testIndirectConditionalJump(IJZ(0), 0, 4, 0.1, 0);
        testIndirectConditionalJump(IJZ(5), 5, 2, 0.2, 0);
        testIndirectConditionalJump(IJZ(10), 10, 3, 0.3, 0);
    }

    @Test
    public void shouldPerformIndirectJGEZ() throws Exception {
        testIndirectConditionalJump(IJGEZ(0), 0, 4, -1, 0);
        testIndirectConditionalJump(IJGEZ(5), 5, 2, -2, 0.5);
        testIndirectConditionalJump(IJGEZ(10), 10, 3, -3, 1);
    }

    @Test
    public void shouldPerformIndirectJLTZ() throws Exception {
        testIndirectConditionalJump(IJLZ(0), 0, 4, 0, -1);
        testIndirectConditionalJump(IJLZ(5), 5, 2, 1, -0.5);
        testIndirectConditionalJump(IJLZ(10), 10, 3, 2, -2);
    }

    @Test
    public void shouldExecuteLoop() throws Exception {
        testLoop(0);
        testLoop(1);
        testLoop(2);
        testLoop(3);
    }

    @Test
    public void shouldStartFromNonZeroAddress() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(4),
                DIGIT(0),
                DIGIT(1),
                DIGIT(0),
                DIGIT(0),
                STOP
        ));

        mk54.setStartAddress(2);
        mk54.execute();
        final double x = mk54.getX();
        assertThat(x, is(100.0));
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
        final double counter = mk54.getMem(register);
        final double accum = mk54.getMem(5);

        assertThat(counter, is(1.0));
        assertThat(accum, is(4.0));
    }

    private static void testIndirectConditionalJump(final String operation,
                                                    final int memRegNo,
                                                    final int memRegValue,
                                                    final double Xvalue1,
                                                    final double Xvalue2) throws Exception {
        final String program = program(
                operation,
                DIGIT(1),
                STOP,
                DIGIT(2),
                STOP
        );
        Mk54Wrapper mk54 = getCompiledInstance(program);

        mk54.setX(Xvalue1);
        mk54.setMem(memRegNo, memRegValue);
        mk54.execute();
        final double x1 = mk54.getX();
        assertThat(x1, is(1.0));

        mk54 = getCompiledInstance(program);
        mk54.setX(Xvalue2);
        mk54.setMem(memRegNo, memRegValue);
        mk54.execute();
        final double x2 = mk54.getX();
        assertThat(x2, is(2.0));
    }
}