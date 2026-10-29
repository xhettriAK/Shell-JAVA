package Execution;

import Builtins.Jobs;
import Builtins.WorkingDirectory;
import Token.Parser;
import Token.TokenType;

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


    private static void configureAndRunProcess(String commandName,String fullcommand,ProcessBuilder processBuilder, String outputFile,
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
         if(runInBackground ) {
            if(Jobs.isJobListEmpty()) {
                job_ID = 1;
            }
            else if( job_ID !=Jobs.getLastJobId()){
                job_ID = Jobs.getLastJobId()+1;
            }
            else{
                job_ID++;
            }
             long pid = process.pid();
             Jobs.putJobsInfo(job_ID, pid, fullcommand, process.isAlive());
             System.out.println("[" + job_ID + "]" + " " + pid);


         }
         else{
             process.waitFor();
         }

    }

    public static boolean isCommandAvailable(String command) {
        return PathUtils.findExecutable(command).isEmpty();
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

    public static void executeExternalCommand( List<String> commandArguments, String outputFile,
                                              String errorFile, boolean appendOutput,
                                              boolean appendError, boolean runInBackground) {

        String commandName = commandArguments.getFirst();
        StringBuilder commandArgs = new StringBuilder();
        for(var i : commandArguments)
        {
            commandArgs.append(i);
        }
       String s=  commandArgs.toString();
        if (isCommandAvailable(commandName)) {
            System.err.println(commandName + ": command not found");
        } else {
            ProcessBuilder processBuilder = new ProcessBuilder(commandArguments).directory(new File(WorkingDirectory.getCurrentDirectory()));
            // directory as cd can change the directory , we can provide new process their  new directory
            try {
                configureAndRunProcess(commandName, s,processBuilder, outputFile, errorFile, appendOutput, appendError, runInBackground);

            } catch (IOException | InterruptedException e) {
                System.err.println(commandName + ": " + e.getMessage());
            }
        }

    }
    public static void executeCommandPipe(List<Parser.CommandInformation>externalRecord, ByteArrayOutputStream customStream
                                           ,boolean isThisLastCommand,String outputFile,boolean appendOutput) {
        List<ProcessBuilder> processBuilders = new ArrayList<>();
        for (int i = 0; i < externalRecord.size(); i++) {

            ProcessBuilder p = new ProcessBuilder(externalRecord.get(i).arguments()).directory(new File(WorkingDirectory.getCurrentDirectory()));
            if(isThisLastCommand && i ==externalRecord.size()-1){
                if(!outputFile.isBlank())
                {
                    if(appendOutput)
                    {
                        p.redirectOutput(ProcessBuilder.Redirect.appendTo(PathUtils.resolveFromCurrentDirectory(outputFile).toFile()));
                    }
                    else {
                        p.redirectOutput(PathUtils.resolveFromCurrentDirectory(outputFile).toFile());
                    }
                }else {
                    p.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                }
            } else {
                p.redirectError(ProcessBuilder.Redirect.INHERIT);
                p.redirectOutput(ProcessBuilder.Redirect.PIPE);

            }
            if (!externalRecord.get(i).errorFile().isBlank()) {
                boolean appendError = externalRecord.get(i).errorType() == TokenType.APPEND_ERR;
                if (appendError) {
                    p.redirectError(ProcessBuilder.Redirect.appendTo(PathUtils.resolveFromCurrentDirectory(externalRecord.get(i).errorFile()).toFile()));
                } else {
                    p.redirectError(PathUtils.resolveFromCurrentDirectory(externalRecord.get(i).errorFile()).toFile());
                }
            }
            processBuilders.add(p);

        }
        try {

            List<Process> processes = ProcessBuilder.startPipeline(processBuilders);
            //Get output of last process;
            if(customStream!=null){
                try (var firstProcessInput = processes.getFirst().getOutputStream()) {
                    customStream.writeTo(firstProcessInput);
                }
            }
            for (Process p : processes) {
                p.waitFor();
            }
        } catch (Exception e) {
            System.err.println(e);
        }

    }




}
