import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class ExternalCommands {

    public static void processCreationFile(ProcessBuilder pb, String fileName) throws IOException,  InterruptedException {
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(FindPath.getAbsolutePath(fileName).toFile());
        Process p = pb.start();
        p.waitFor();
    }
    public static void processCreation(ProcessBuilder pb) throws IOException,  InterruptedException {

        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        p.waitFor();


    }
    public static boolean wasCommandFound(String command) {

        return !FindPath.checkExecutable(command).isEmpty();
    }
    public static void runCommands(String command, List<String> userArgument,  String fileName,boolean isFileNameProvided) {
        if (!wasCommandFound(command)) {
            System.err.println(command + ": command not found");
        }
        else {
            StringBuilder argumentBuilder = new StringBuilder();
            for(var i: userArgument)
            {
                argumentBuilder.append(i);

            }

            String argument = argumentBuilder.toString();

            Path currentPath = Path.of(BuiltinCommands.getCurrentPath());
            ProcessBuilder pb = new ProcessBuilder(command, argument).directory(currentPath.toFile());
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                if (isFileNameProvided) {
                    processCreationFile( pb, fileName);
                } else {
                    processCreation( pb);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println(command +": "+e.getMessage());
            }
        }
    }
}
