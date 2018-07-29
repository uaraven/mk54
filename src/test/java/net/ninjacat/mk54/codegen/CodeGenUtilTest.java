package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.exceptions.InvalidJumpTargetException;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CodeGenUtilTest {

    @Test
    public void shouldParseAddressesCorrectly() {
        assertThat(CodeGenUtil.parseAddress("11"), is(11));
        assertThat(CodeGenUtil.parseAddress("99"), is(99));
        assertThat(CodeGenUtil.parseAddress("a4"), is(104));
    }

    @Test(expected = InvalidJumpTargetException.class)
    public void shouldFailOnIncompleteAddress() {
        CodeGenUtil.parseAddress("1");

        fail("Should have failed");
    }

    @Test(expected = InvalidJumpTargetException.class)
    public void shouldFailOnTooLongAddress() {
        CodeGenUtil.parseAddress("123");

        fail("Should have failed");
    }

}