package Execution;
import Builtins.WorkingDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class PathUtils {
    public static String findExecutable(String fileName) {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(java.io.File.pathSeparator);
        for (var directory : paths) {
            String candidatePath = directory + "/" + fileName;
            Path path = Path.of(candidatePath);
            if (Files.exists(path)) {
                if (Files.isExecutable(path)) {
                    return candidatePath;
                }
            }
        }
        return "";
    }
    public static Set<String> findFileCompletions(String directory, String prefix)
    {    boolean addBackslash = false;
        Set<String> matches = new TreeSet<>();
        Path currentDirectory = Path.of(WorkingDirectory.getCurrentDirectory()).resolve(Path.of(directory));
        try (Stream<Path> stream = Files.list(currentDirectory)) {
             List<Path> paths = stream.toList();
             for (Path candidate : paths) {
                 if (Files.isDirectory(candidate)) {
                     addBackslash = true;
                 }
                 String fileName = candidate.getFileName().toString();
                 if (fileName.startsWith(prefix)) {
                     if (addBackslash) {
                         matches.add(fileName + "/");
                     } else {
                         matches.add(fileName);
                     }
                 }
                 addBackslash=false;
             }
        } catch (IOException e) {
                    System.out.print("");//skipping unreadable directories;
        }

        return matches;

    }

    public static Path resolveFromCurrentDirectory(String fileName) {

        Path currentDirectory = Path.of(WorkingDirectory.getCurrentDirectory());

        return currentDirectory.resolve(fileName);
    }

    public static void printExecutablePath(String commandName) {
        String executablePath = PathUtils.findExecutable(commandName);
        if (!executablePath.isEmpty()) {
            System.out.println(commandName + " is " + executablePath);
        } else {
            System.err.println(commandName + ": not found");
        }
    }
}
