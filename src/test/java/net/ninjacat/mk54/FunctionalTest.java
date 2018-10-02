package net.ninjacat.mk54;

import net.ninjacat.mk54.test.CodeGenFixtures;
import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Programs tested here were validated on actual MK-52 calculator
 */
public class FunctionalTest {

    @Test
    public void calculateAreaOfCircle() throws Exception {
        final Mk54Wrapper mk54 = compileResource("/functional/circle_area.mk");
        mk54.setX(4f);

        mk54.execute();

        assertThat(mk54.getX(), is(closeTo(12.566, 0.01)));
    }

    @Test
    public void testSequenceOfCalculations() throws Exception {
        final Mk54Wrapper mk54 = compileResource("/functional/useless_calculations.mk");
        mk54.setX(5f);

        mk54.execute();

        assertThat(mk54.getX(), is(closeTo(1.79176, 0.00001)));
    }

    @Test
    public void testIndirectJumps() throws Exception {
        final Mk54Wrapper mk54 = compileResource("/functional/indirect_jumps.mk");
        mk54.setMem(3, 5);
        mk54.setMem(4, 7);
        mk54.setMem(10, 12);

        mk54.execute();

        assertThat(mk54.getX(), is(18.0));
        assertThat(mk54.getMem(3), is(4.0));
        assertThat(mk54.getMem(4), is(8.0));
        assertThat(mk54.getMem(10), is(12.0));
    }

    @Test
    public void testQuadraticEquation() throws Exception {
        final Mk54Wrapper mk54 = compileResource("/functional/quadratic_eq.mk");
        mk54.setMem(10, 3);
        mk54.setMem(11, 2);
        mk54.setMem(12, -1);

        mk54.execute();

        assertThat(mk54.getX(), is(-1.0));
        assertThat(mk54.getMem(1), closeTo(0.3333333, 1e-7));
    }


    private static Mk54Wrapper compileResource(final String resource) throws Exception {
        final String program = Resources.loadProgram(resource);
        final String mk54Code = new Mk54CodeGenerator().compile(program);

        return CodeGenFixtures.getCompiledInstance(mk54Code);
    }
}
