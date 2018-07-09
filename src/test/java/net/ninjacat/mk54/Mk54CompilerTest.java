package net.ninjacat.mk54;

import com.google.common.io.CharStreams;
import net.ninjacat.mk54.exceptions.RuntimeIOException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Mk54CompilerTest {

    private Mk54Compiler compiler;

    @Before
    public void setUp() {
        this.compiler = new Mk54Compiler();
    }

    @Test
    public void shouldCompileSimpleProgram() {
        final String program = loadProgram("/compiler_test.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), arrayWithSize(39));
    }

    @Test
    public void shouldParseProgramsWithLatinMnemonics() {
        final String program = loadProgram("/test_latin.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), arrayWithSize(8));
        assertThat(mkCode, is("01 02 0E 01 02 10 40 60"));
    }

    @Test
    public void shouldParseProgramsWithCyrillicMnemonics() {
        final String program = loadProgram("/test_cyrillic.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), arrayWithSize(9));
        assertThat(mkCode, is("01 02 0E 01 02 10 40 60 0F"));
    }

    private String loadProgram(final String resource) {
        try (final InputStreamReader is = new InputStreamReader(getClass().getResourceAsStream(resource))) {
            return CharStreams.toString(is);
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}