package net.ninjacat.mk54;

import com.google.common.io.CharStreams;
import net.ninjacat.mk54.exceptions.RuntimeIOException;

import java.io.IOException;
import java.io.InputStreamReader;


final class Resources {

    private Resources() {
    }

    static String loadProgram(final String resource) {
        try (final InputStreamReader is = new InputStreamReader(Resources.class.getResourceAsStream(resource))) {
            return CharStreams.toString(is);
        } catch (final IOException e) {
            throw new RuntimeIOException(e);
        }
    }
}
