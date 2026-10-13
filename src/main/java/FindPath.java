
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public static void createFileforBuiltIN(String userArgument, String filename)
    {
        Path path = getAbsolutePath(filename);
        try{
            Files.writeString(path,userArgument);
        } catch (IOException e) {
            System.out.println(filename+": no file or directory");
        }
    }
    public static Path getAbsolutePath(String filename) {

        Path currentPath = Path.of(BuiltinCommands.getCurrentPath());

        return currentPath.resolve(filename);
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
