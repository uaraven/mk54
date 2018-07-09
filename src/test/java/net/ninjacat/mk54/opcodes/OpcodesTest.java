package net.ninjacat.mk54.opcodes;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OpcodesTest {

    private Opcodes opcodes;

    @Before
    public void setUp() throws Exception {
        this.opcodes = new Opcodes();
    }

    @Test
    public void shouldFailOnUnknownMnemonic() {
        final Optional<String> opcode = this.opcodes.findOpcode("k прг");

        assertThat(opcode.isPresent(), is(false));
    }

    @Test
    public void shouldFindOpcode() {
        final Optional<String> opcode = this.opcodes.findOpcode("xп0");

        assertThat(opcode.isPresent(), is(true));
        assertThat(opcode.get(), is("40"));
    }

    @Test
    public void shouldFindOpcodeAfterNormalizing() {
        final Optional<String> opcode = this.opcodes.findOpcode("хп0");

        assertThat(opcode.isPresent(), is(true));
        assertThat(opcode.get(), is("40"));
    }

}