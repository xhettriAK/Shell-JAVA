package Builtins;

import java.nio.file.Path;

public class WorkingDirectory implements BuiltInCommands {
    private static String currentDirectory = Path.of("").toAbsolutePath().toString();
    public  static String getCurrentDirectory()
    {
        return currentDirectory;

    }
    public static void setCurrentDirectory(String currentDirectory) {
        WorkingDirectory.currentDirectory = currentDirectory;
    }
}
