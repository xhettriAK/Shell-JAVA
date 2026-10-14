import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class ExternalCommands {

    public static void processCreationFile(ProcessBuilder pb, String outputFile, String errorFile) throws IOException,  InterruptedException {

        if(!outputFile.isBlank() && !errorFile.isBlank()) {
            pb.redirectError(FindPath.getAbsolutePath(errorFile).toFile());
            pb.redirectOutput(FindPath.getAbsolutePath(outputFile).toFile());
        }
        else if(!outputFile.isBlank() && errorFile.isBlank())
        {
            pb.redirectOutput(FindPath.getAbsolutePath(outputFile).toFile());
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
        else if(outputFile.isBlank() && !errorFile.isBlank())
        {
            pb.redirectError(FindPath.getAbsolutePath(errorFile).toFile());
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
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
    public static void runCommands(String command, List<String> userList, String outputFile, String errorFile) {
        if (!wasCommandFound(command)) {
            System.err.println(command + ": command not found");
        }
        else {
            Path currentPath = Path.of(BuiltinCommands.getCurrentPath());
            ProcessBuilder pb = new ProcessBuilder( userList);
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                if (!outputFile.isBlank() ||!errorFile.isBlank()) {
                    processCreationFile( pb, outputFile,errorFile);
                } else {
                    processCreation( pb);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println(command +": "+e.getMessage());
            }
        }
    }
}
