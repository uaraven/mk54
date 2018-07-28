package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.exceptions.InvalidJumpTargetException;
import org.junit.Test;

import static net.ninjacat.mk54.codegen.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.codegen.CodeGenFixtures.program;
import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ControlGenTest {

    @Test
    public void shouldStopProgram() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                DIGIT(2),
                RUN_STOP,
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
                "04",
                DIGIT(2),
                RUN_STOP,
                DIGIT(9),
                RET
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(92f));
    }


    @Test
    public void shouldSupportNestedSubroutines() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                GOSUB,
                "04",
                DIGIT(2),
                RUN_STOP,
                DIGIT(9),
                GOSUB,
                "08",
                RET,
                DIGIT(1),
                RET
        ));

        mk54.execute();
        final float x = mk54.getX();

        assertThat(x, is(912f));
    }

    @Test(expected = InvalidJumpTargetException.class)
    public void shouldFailJumpBeyonLastOperation() throws Exception {
        getCompiledInstance(program(
                GOTO,
                "44"
        ));
        fail("Should have failed during code generation");
    }

}