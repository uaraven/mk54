package net.ninjacat.mk54;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Charsets;
import net.ninjacat.mk54.codegen.CodeGenerator;
import net.ninjacat.mk54.exceptions.UnknownCommandException;
import net.ninjacat.mk54.opcodes.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Compiles MK-series (and B3-34) mnemonic programs into executable Java JAR file
 */
public class Mk54CodeGenerator {

    private static final Pattern ADDRESS = Pattern.compile("^(\\d{2}\\.).*");
    private final Opcodes opcodes;

    Mk54CodeGenerator() {
        this.opcodes = new Opcodes();
    }

    public static void main(final String[] args) {

        final String version = readVersion();
        System.out.println(version);

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

    private static String readVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(Mk54CodeGenerator.class.getResourceAsStream("/version.properties"));

            return String.format("mk54 compiler v%s", properties.getProperty("version", "Unknown"));
        } catch (final Exception ex) {
            return
                    "";
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

        writeOutJar(classBytes, program, compilationSettings);
    }

    private static void writeOutJar(final byte[] classBytes,
                                    final String source,
                                    final Settings compilationSettings) throws IOException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(new Attributes.Name("Manifest-Version"), "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Main-Class"), "net.ninjacat.mk54.Mk54");
        try (final JarOutputStream jarFile = new JarOutputStream(new FileOutputStream(compilationSettings.getOutputFile()), manifest)) {
            final JarEntry classEntry = new JarEntry("net/ninjacat/mk54/Mk54.class");
            final long time = System.currentTimeMillis();
            classEntry.setTime(time);
            jarFile.putNextEntry(classEntry);
            jarFile.write(classBytes);
            jarFile.closeEntry();

            final JarEntry srcEntry = new JarEntry(compilationSettings.getSourceFileName());
            srcEntry.setTime(time);
            jarFile.putNextEntry(srcEntry);
            jarFile.write(source.getBytes(Charsets.UTF_8));
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
