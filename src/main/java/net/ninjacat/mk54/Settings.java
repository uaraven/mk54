package net.ninjacat.mk54;

import com.beust.jcommander.Parameter;

import java.nio.file.Paths;

@SuppressWarnings({"unused", "FieldMayBeFinal", "FieldCanBeLocal"})
class Settings {

    @Parameter(description = "<source file>", required = true)
    private String programFile;

    @Parameter(names = {"-o", "--output"}, description = "File name for output jar file")
    private String outputFile = null;

    @Parameter(names = {"-h", "--help"}, description = "Display this help screen", help = true)
    private boolean showHelp = false;

    @Parameter(names = {"-d", "--debug"}, description = "Include debug state printouts for each MK program step", hidden = true)
    private boolean generateDebug = false;

    @Parameter(names = {"-v", "--verbose"}, description = "Display more information during compilation process")
    private boolean verbose = false;

    String getProgramFile() {
        return this.programFile;
    }

    String getOutputFile() {
        if (this.outputFile == null) {
            this.outputFile = stripFileExtension(this.programFile) + ".jar";
        }
        return this.outputFile;
    }

    String getSourceFileName() {
        return Paths.get(this.programFile).getFileName().toString();
    }

    private static String stripFileExtension(final String fileName) {
        if (fileName == null) {
            return "";
        }
        final int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return fileName; // empty extension
        }
        return fileName.substring(0, lastIndexOf);
    }

    boolean isShowHelp() {
        return this.showHelp;
    }

    boolean isGenerateDebug() {
        return this.generateDebug;
    }

    boolean isVerbose() {
        return this.verbose;
    }
}
