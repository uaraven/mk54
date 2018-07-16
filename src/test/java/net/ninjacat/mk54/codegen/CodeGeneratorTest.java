package net.ninjacat.mk54.codegen;

import org.junit.Test;

import static net.ninjacat.mk54.opcodes.Opcode.FAIL1;
import static org.junit.Assert.fail;

public class CodeGeneratorTest {

    @Test(expected = Exception.class)
    public void shouldThrowError() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                FAIL1
        ));

        mk54.execute();

        fail("Should throw exception");
    }
}