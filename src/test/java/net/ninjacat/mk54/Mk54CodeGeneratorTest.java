package net.ninjacat.mk54;

import net.ninjacat.mk54.exceptions.UnknownCommandException;
import org.junit.Before;
import org.junit.Test;

import static net.ninjacat.mk54.Resources.loadProgram;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Mk54CodeGeneratorTest {

    private Mk54CodeGenerator compiler;

    @Before
    public void setUp() {
        this.compiler = new Mk54CodeGenerator();
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

    @Test
    public void shouldParseProgramsWithoutAddresses() {
        final String program = loadProgram("/test_no_address.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), arrayWithSize(8));
        assertThat(mkCode, is("01 02 0E 01 02 10 40 60"));
    }

    @Test
    public void shouldParseProgramsWithMixedAddresses() {
        final String program = loadProgram("/test_mixed_address.mk");
        final String mkCode = this.compiler.compile(program);

        assertThat(mkCode.split(" "), arrayWithSize(8));
        assertThat(mkCode, is("01 02 0E 01 02 10 40 60"));
    }

    @Test(expected = UnknownCommandException.class)
    public void shouldFailToCompileInvalidProgram() {
        final String program = "F invalid";
        this.compiler.compile(program);
    }

}