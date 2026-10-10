
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FindPath {
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

    public static void filePath(String command) {
        String path = checkExecutable(command);

        if (!path.isEmpty()) {
            System.out.println(command + " is " + path);
        } else {
            System.out.println(command + ": not found");
        }
    }
}
