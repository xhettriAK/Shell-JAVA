package Builtins;

import java.io.IOException;
import java.nio.file.Path;

//current Directory
public class CD  implements BuiltInCommands{
     static void changeDirectory(String directoryArgument)
    {
        Path targetPath;
        String userName = System.getenv("USER");

        if (directoryArgument.equals("~" + userName)) {
            targetPath = Path.of(System.getenv("HOME"));
        }
        else if (directoryArgument.startsWith("~")) {
            targetPath = Path.of(System.getenv("HOME") + directoryArgument.substring(1));
        }
        else {
            targetPath = Path.of(directoryArgument);
        }
        Path absolutePath = Path.of(WorkingDirectory.getCurrentDirectory()).resolve(targetPath);
        //ignore the tempPath if it is absolute path
        boolean directoryExists = false;

        try{
            absolutePath = absolutePath.toRealPath();
            directoryExists = true;
        } catch (IOException e) {
            System.err.println("cd: " + directoryArgument + ": No such file or directory");
        }
        if (directoryExists) {
            WorkingDirectory.setCurrentDirectory(absolutePath.toString());
        }
    }
}
