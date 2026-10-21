import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ExternalCommandRunner {


    public static Set<String> findExternalCommandCompletions(String prefix)
    {
        Set<String> matches = new TreeSet<>();
        String pathEnv = System.getenv("PATH");
        String[] directories = pathEnv.split(File.pathSeparator);
        for (String directory : directories) {
            Path directoryPath = Path.of(directory);
            if (Files.isDirectory(directoryPath)) {
                try (Stream<Path> stream = Files.list(directoryPath)) {
                    List<Path> entries = stream.toList();
                    for (Path entry : entries) {
                        String commandName = entry.getFileName().toString();
                        if (commandName.startsWith(prefix) && Files.isRegularFile(entry) && Files.isExecutable(entry)) {
                                    matches.add(commandName);
                        }
                    }
                } catch (IOException e) {
                    System.out.print("");//skipping unreadable directories;
                }
            }
        }
        return matches;
    }


    private static void configureAndRunProcess(ProcessBuilder processBuilder, String outputFile, String errorFile, boolean appendOutput, boolean appendError) throws IOException, InterruptedException {
        if(!outputFile.isBlank())
        {
            if(appendOutput)
            {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(PathUtils.resolveFromCurrentDirectory(outputFile).toFile()));
            }
            else {
                processBuilder.redirectOutput(PathUtils.resolveFromCurrentDirectory(outputFile).toFile());
            }
        }
        else {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
         if(!errorFile.isBlank())
        {
            if(appendError)
            {
                processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(PathUtils.resolveFromCurrentDirectory(errorFile).toFile()));
            }
            else{
                processBuilder.redirectError(PathUtils.resolveFromCurrentDirectory(errorFile).toFile());
            }
        } else{
             processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
         }

        Process process = processBuilder.start();
        process.waitFor();
    }
    private static boolean isCommandAvailable(String command) {

        return !PathUtils.findExecutable(command).isEmpty();
    }
    public static String executeCompletorCommand(StringBuilder input, String path, String argv1, String argv2, String argv3)  {

        ProcessBuilder processBuilder = new ProcessBuilder(path,argv1, argv2,argv3);
         Map<String,String> env = processBuilder.environment();
         env.put("COMP_LINE", input.toString());
         env.put("COMP_POINT",String.valueOf(input.length()));

        StringBuilder output= new StringBuilder();
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while( (line=reader.readLine())!=null) {
                    output.append(line);
                }
            }
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
            return output.toString().strip();
    }
    public static void executeExternalCommand(String command, List<String> commandArguments, String outputFile, String errorFile, boolean appendOutput, boolean appendError) {
        if (!isCommandAvailable(command)) {
            System.err.println(command + ": command not found");
        }
        else {
            ProcessBuilder processBuilder = new ProcessBuilder(commandArguments);
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                configureAndRunProcess(processBuilder, outputFile, errorFile, appendOutput, appendError);

            } catch (IOException | InterruptedException e) {
                System.err.println(command +": "+e.getMessage());
            }
        }
    }
}
