package Execution;

import Builtins.Jobs;
import com.google.common.collect.Iterables;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ExternalCommandRunner {

    static int job_ID = 0;
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


    private static void configureAndRunProcess(String commandName,ProcessBuilder processBuilder, String outputFile,
                                               String errorFile, boolean appendOutput, boolean appendError, boolean runInBackground) throws IOException, InterruptedException {
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
         if(runInBackground) {
             job_ID++;
             long pid = process.pid();
             Jobs.putjobsInfo(job_ID,pid,commandName,process.isAlive());
             System.out.println("["+job_ID+"]"+" "+ pid);
         }
         else{
             process.waitFor();
         }

    }
    private static boolean isCommandAvailable(String command) {

        return !PathUtils.findExecutable(command).isEmpty();
    }
    public static Set<String> executeCompletorCommand(StringBuilder input, String path, String argv1, String argv2, String argv3)  {

        ProcessBuilder processBuilder = new ProcessBuilder(path,argv1, argv2,argv3);
         Map<String,String> env = processBuilder.environment();
         env.put("COMP_LINE", input.toString());
         env.put("COMP_POINT",String.valueOf(input.length()));

        Set<String>output = new TreeSet<>();
        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while( (line=reader.readLine())!=null) {
                    output.add(line);
                }
            }
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
            return output;
    }
    public static void executeExternalCommand(String command, List<String> commandArguments, String outputFile,
                                              String errorFile, boolean appendOutput, boolean appendError, boolean runInBackground) {
        String commandName = "";
        StringBuilder commandBuilder = new StringBuilder();
        for(var i : commandArguments)
        {
            commandBuilder.append(i).append(" ");
        }
        commandName = commandBuilder.toString();
        if (!isCommandAvailable(command)) {
            System.err.println(command + ": command not found");
        }
        else {
            ProcessBuilder processBuilder = new ProcessBuilder(commandArguments);
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                configureAndRunProcess( commandName,processBuilder, outputFile, errorFile, appendOutput, appendError,runInBackground);

            } catch (IOException | InterruptedException e) {
                System.err.println(command +": "+e.getMessage());
            }
        }
    }
}
