
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public  class FilePath {
    public static String checkExecutable(String filename) {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(java.io.File.pathSeparator);
        for (var i : paths) {
            String temp = i + "/" + filename;
            Path path = Path.of(temp);
            if (Files.exists(path)) {
                if (Files.isExecutable(path)) {
                    return temp;
                }
            }
        }
        return "";
    }
    public static Set<String> tabCompletetionForFile(String prefix)
    {
        Set<String>list = new TreeSet<>();
        Path path = Path.of(BuiltinCommands.getCurrentPath());
        try (Stream<Path> stream = Files.list(path)) { //files.list return name
             List<Path> filename =stream.toList();
             for (Path value : filename) {
                 String name = value.getFileName().toString();
                 if (name.startsWith(prefix) && Files.isRegularFile(value)) {
                     list.add(name);
                 }
             }
        } catch (IOException e) {
                    System.out.print("");//skipping unreadable directories;
        }

        return list;

    }

    public static Path getAbsolutePath(String filename) {

        Path currentPath = Path.of(BuiltinCommands.getCurrentPath());

        return currentPath.resolve(filename);
    }

    public static void filePath(String command) {
        String path = FilePath.checkExecutable(command);
        if (!path.isEmpty()) {
            System.out.println(command + " is " + path);
        } else {
            System.err.println( command + ": not found");
        }
    }
}
