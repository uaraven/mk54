package net.ninjacat.mk54.codegen;

import net.ninjacat.mk54.exceptions.UnknownOperationException;
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

    @Test(expected = UnknownOperationException.class)
    public void shouldFailOnUnknownOperation() throws Exception {
        final Mk54Wrapper mk54 = CodeGenFixtures.getCompiledInstance(CodeGenFixtures.program(
                "FF"
        ));

        mk54.execute();

        fail("Should throw exception");
    }
}