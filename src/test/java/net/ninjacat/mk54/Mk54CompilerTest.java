package net.ninjacat.mk54;

import com.google.common.io.CharStreams;
import net.ninjacat.mk54.exceptions.RuntimeIOException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertThat;

public class Mk54CompilerTest {

    private Mk54Compiler compiler;

    @Before
    public void setUp() throws Exception {
        this.compiler = new Mk54Compiler();
    }

    @Test
    public void shouldCompileSimpleProgram() {
        final String program = loadProgram("/compiler_test.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), Matchers.arrayWithSize(39));
    }

    private String loadProgram(final String resource) {
        try (final InputStreamReader is = new InputStreamReader(getClass().getResourceAsStream(resource))) {
            return CharStreams.toString(is);
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}