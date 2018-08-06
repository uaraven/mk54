package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.*;
import static net.ninjacat.mk54.test.CodeGenFixtures.getCompiledInstance;
import static net.ninjacat.mk54.test.CodeGenFixtures.program;
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
        final double m0 = mk54.getMem(0);

        assertThat(m0, is(1.0));
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
            final double m = mk54.getMem(i);

            assertThat(String.format("Register %d should contain %d", i, i), m, is((double) i));
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
            final double x = mk54.getX();

            assertThat(String.format("Register %d should contain %d", i, i), x, is((double) i));
        }
    }

    @Test
    public void shouldDecreaseMemValueAndStoreToMemory() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(4),
                ISTO(0),
                STOP
        ));

        mk54.setMem(0, 2);
        mk54.execute();
        final double m1 = mk54.getMem(1);
        final double m2 = mk54.getMem(2);

        assertThat(m1, is(54.0));
        assertThat(m2, is(0.0));
    }

    @Test
    public void shouldIncreaseMemValueAndStoreToMemory() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(4),
                ISTO(5),
                STOP
        ));

        mk54.setMem(5, 2);
        mk54.execute();
        final double m2 = mk54.getMem(2);
        final double m3 = mk54.getMem(3);

        assertThat(m3, is(54.0));
        assertThat(m2, is(0.0));
    }

    @Test
    public void shouldPerformIndirectStore() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                DIGIT(5),
                DIGIT(4),
                ISTO(10),
                STOP
        ));

        mk54.setMem(10, 2);
        mk54.execute();
        final double m2 = mk54.getMem(2);

        assertThat(m2, is(54.0));
    }

    @Test
    public void shouldDecreaseMemValueAndReadMemory() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                IRCL(0),
                STOP
        ));

        mk54.setMem(1, 54f);
        mk54.setMem(0, 2);
        mk54.execute();
        final double m0 = mk54.getMem(0);
        final double x = mk54.getX();

        assertThat(m0, is(1.0));
        assertThat(x, is(54.0));
    }

    @Test
    public void shouldIncreaseMemValueAndReadMemory() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                IRCL(5),
                STOP
        ));

        mk54.setMem(5, 2);
        mk54.setMem(3, 54f);
        mk54.execute();
        final double m2 = mk54.getMem(5);
        final double x = mk54.getX();

        assertThat(x, is(54.0));
        assertThat(m2, is(3.0));
    }

    @Test
    public void shouldPerformIndirectRead() throws Exception {
        final Mk54Wrapper mk54 = getCompiledInstance(program(
                IRCL(10),
                STOP
        ));

        mk54.setMem(10, 2);
        mk54.setMem(2, 54f);
        mk54.execute();
        final double m2 = mk54.getMem(10);
        final double x = mk54.getX();

        assertThat(m2, is(2.0));
        assertThat(x, is(54.0));
    }
}