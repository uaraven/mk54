package net.ninjacat.mk54.codegen;

import org.junit.Test;

import static net.ninjacat.mk54.codegen.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.codegen.CodeGenFixtures.program;
import static net.ninjacat.mk54.opcodes.Opcode.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MemoryGenTest {

    @Test
    public void shouldStoreXtoMemory0() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(1),
                STO(0)
        ));

        mk54.execute();
        final float m0 = mk54.getMem(0);

        assertThat(m0, is(1f));
    }

    @Test
    public void shouldStoreXtoMemory() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(0),
                STO(0),
                DIGIT(1),
                STO(1),
                DIGIT(2),
                STO(2),
                DIGIT(3),
                STO(3),
                DIGIT(4),
                STO(4),
                DIGIT(5),
                STO(5),
                DIGIT(6),
                STO(6),
                DIGIT(7),
                STO(7),
                DIGIT(8),
                STO(8),
                DIGIT(9),
                STO(9),
                DIGIT(1),
                DIGIT(0),
                STO(10),
                DIGIT(1),
                DIGIT(1),
                STO(11),
                DIGIT(1),
                DIGIT(2),
                STO(12),
                DIGIT(1),
                DIGIT(3),
                STO(13),
                DIGIT(1),
                DIGIT(4),
                STO(14)
        ));

        mk54.execute();
        for (int i = 0; i < 15; i++) {
            final float m = mk54.getMem(i);

            assertThat(String.format("Register %d should contain %d", i, i), m, is((float) i));
        }
    }

    @Test
    public void shouldReadFromMemory() throws Exception {
        Mk54Wrapper mk54;
        for (int i = 0; i < 15; i++) {
            mk54 = getCompiledInstance(program(
                    RCL(i)
            ));

            mk54.setMem(i, i);

            mk54.execute();
            final float x = mk54.getX();

            assertThat(String.format("Register %d should contain %d", i, i), x, is((float) i));
        }
    }


}