package net.ninjacat.mk54;

import net.ninjacat.mk54.test.CodeGenFixtures;
import net.ninjacat.mk54.test.Mk54Wrapper;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FunctionalTest {

    @Test
    public void calculateAreaOfCircle() throws Exception {
        final Mk54Wrapper mk54 = compileResource();
        mk54.setX(4f);

        mk54.execute();

        assertThat((double) mk54.getX(), is(closeTo(12.566, 0.01)));
    }

    private static Mk54Wrapper compileResource() throws Exception {
        final String program = Resources.loadProgram("/functional/circle_area.mk");
        final String mk54Code = new Mk54CodeGenerator().compile(program);

        return CodeGenFixtures.getCompiledInstance(mk54Code);
    }
}
