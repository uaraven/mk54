package net.ninjacat.mk54;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import net.ninjacat.mk54.codegen.CodeGenerator;
import net.ninjacat.mk54.exceptions.UnknownCommandException;
import net.ninjacat.mk54.opcodes.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Compiles MK-series (and B3-34) mnemonic programs into "binary" presentation.
 */
public class Mk54CodeGenerator {

    private static final Pattern ADDRESS = Pattern.compile("^(\\d{2}\\.).*");
    private static final String VERSION = "0.1";
    private final Opcodes opcodes;

    Mk54CodeGenerator() {
        this.opcodes = new Opcodes();
    }

    public static void main(final String[] args) {

        final Settings compilationSettings = new Settings();
        final JCommander jc = JCommander.newBuilder()
                .programName("mk54")
                .addObject(compilationSettings)
                .build();
        try {
            jc.parse(args);
        } catch (final ParameterException ex) {
            jc.usage();
            return;
        }

        if (compilationSettings.isShowHelp()) {
            jc.usage();
            return;
        }

        try {
            final Mk54CodeGenerator compiler = new Mk54CodeGenerator();
            compiler.generateJar(compilationSettings);
        } catch (final Exception ex) {
            System.err.println("Failure: " + ex.getMessage());
        }
    }

    private void generateJar(final Settings compilationSettings) throws IOException {
        if (compilationSettings.isVerbose()) {
            System.out.println("Reading source file " + compilationSettings.getProgramFile());
        }
        final String program = String.join("\n", Files.readAllLines(Paths.get(compilationSettings.getProgramFile())));

        if (compilationSettings.isVerbose()) {
            System.out.println("Translating into MK machine code");
        }
        final String mkCode = compile(program);

        if (compilationSettings.isVerbose()) {
            System.out.println("Translating into JVM byte code");
        }
        final CodeGenerator codeGenerator = new CodeGenerator(compilationSettings.isGenerateDebug());
        final byte[] classBytes = codeGenerator.compile(mkCode);

        if (compilationSettings.isVerbose()) {
            System.out.println("Writing JAR file " + compilationSettings.getOutputFile());
        }

        writeOutJar(classBytes, compilationSettings);
    }

    private static void writeOutJar(final byte[] classBytes, final Settings compilationSettings) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(new Attributes.Name("Manifest-Version"), "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Main-Class"), "net.ninjacat.mk54.Mk54");
        try (final JarOutputStream jarFile = new JarOutputStream(new FileOutputStream(compilationSettings.getOutputFile()), manifest)) {
            final JarEntry jarEntry = new JarEntry("net/ninjacat/mk54/Mk54.class");
            jarEntry.setTime(System.currentTimeMillis());
            jarFile.putNextEntry(jarEntry);
            jarFile.write(classBytes);
            jarFile.closeEntry();
        }
    }


    private static String stripAddress(final String line) {
        final Matcher matcher = ADDRESS.matcher(line);
        if (matcher.matches()) {
            return line.substring(matcher.group(1).length()).trim();
        } else {
            return line.trim();
        }
    }

    /**
     * Converts program mnemonics into "binary" hex code
     *
     * @param input Program source
     * @return String containing hex codes of operations
     */
    String compile(final String input) {
        final String[] keys = input.split("\n");
        return Arrays.stream(keys)
                .map(Mk54CodeGenerator::stripAddress)
                .map(key -> this.opcodes.findOpcode(key).orElseThrow(() -> new UnknownCommandException(key)))
                .collect(Collectors.joining(" "));
    }

}
