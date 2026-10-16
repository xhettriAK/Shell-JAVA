import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ExternalCommands {


    public static Set<String>  tabCompletionForExternal(String user)
    {
        Set<String>list = new HashSet<>();
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);
        String temp;
        for (String path : paths) {
            Path p = Path.of(path);
            if (Files.isDirectory(p)) {
                try (Stream<Path> stream = Files.list(p).sorted()) {
                    List<Path> filename =stream.toList();
                    for (Path value : filename) {
                        String name = value.getFileName().toString();
                        if (name.startsWith(user) && Files.isRegularFile(value) && Files.isExecutable(value)) {
                                    list.add(name);
                        }
                    }
                } catch (IOException e) {
                    System.out.print("");//skipping unreadable directories;
                }
            }
        }
        if(list.isEmpty()) {
            return null;
        }
        return list;

    }

    private static void processCreation(ProcessBuilder pb, String outputFile, String errorFile,boolean appendOutput, boolean appendError) throws IOException,  InterruptedException {
        if(!outputFile.isBlank())
        {
            if(appendOutput)
            {
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(FindPath.getAbsolutePath(outputFile).toFile()));
            }
            else {
                pb.redirectOutput(FindPath.getAbsolutePath(outputFile).toFile());
            }
        }
        else {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
         if(!errorFile.isBlank())
        {
            if(appendError)
            {
                pb.redirectError(ProcessBuilder.Redirect.appendTo(FindPath.getAbsolutePath(errorFile).toFile()));
            }
            else{
                pb.redirectError(FindPath.getAbsolutePath(errorFile).toFile());
            }
        } else{
             pb.redirectError(ProcessBuilder.Redirect.INHERIT);
         }

        Process p = pb.start();
        p.waitFor();
    }
    private static boolean wasCommandFound(String command) {

        return !FindPath.checkExecutable(command).isEmpty();
    }
    public static void runCommands(String command, List<String> userList, String outputFile, String errorFile, boolean appendOutput, boolean appendError) {
        if (!wasCommandFound(command)) {
            System.err.println(command + ": command not found");
        }
        else {
            ProcessBuilder pb = new ProcessBuilder( userList);
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                processCreation( pb, outputFile,errorFile,appendOutput,appendError);

            } catch (IOException | InterruptedException e) {
                System.err.println(command +": "+e.getMessage());
            }
        }
    }
}
