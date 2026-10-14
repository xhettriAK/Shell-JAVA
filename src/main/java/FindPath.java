
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;

public  class FindPath {
    public static String checkExecutable(String filename) {
        String pathEnv = System.getenv("PATH");
        String[] paths = pathEnv.split(File.pathSeparator);
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

    public static Path getAbsolutePath(String filename) {

        Path currentPath = Path.of(BuiltinCommands.getCurrentPath());

        return currentPath.resolve(filename);
    }

    public static void filePath(String command) {
        String path = FindPath.checkExecutable(command);
        if (!path.isEmpty()) {
            System.out.println(command + " is " + path);
        } else {
            System.err.println( command + ": not found");
        }
    }
}
