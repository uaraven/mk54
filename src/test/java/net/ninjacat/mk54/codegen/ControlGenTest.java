package net.ninjacat.mk54.codegen;

import org.junit.Test;

import static net.ninjacat.mk54.codegen.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.codegen.CodeGenFixtures.program;
import static net.ninjacat.mk54.opcodes.Opcode.DIGIT;
import static net.ninjacat.mk54.opcodes.Opcode.RUN_STOP;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
}