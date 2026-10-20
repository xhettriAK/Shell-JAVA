import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BuiltInCommands {

    public final static String[] BUILT_IN_COMMANDS = {"echo", "exit", "pwd", "type", "cd","complete"};

    public static String completeBuiltInCommand(String commandPrefix)
    {
        for (var command : BUILT_IN_COMMANDS)
        {
            if (command.startsWith(commandPrefix))
            {
                return command.substring(commandPrefix.length());
            }
        }
        return null;
    }
    private static String currentDirectory = Path.of("").toAbsolutePath().toString();

    public static String getCurrentDirectory()
    {
        return currentDirectory;

    }
    public static boolean isBuiltInCommand(String command)
    {
        for (var builtInCommand : BUILT_IN_COMMANDS) {
            if (command.equals(builtInCommand)) {
                return true;
            }
        }
        return false;
    }
    static void executeBuiltInCommand(String command, List<String> arguments)
    {
        StringBuilder argumentsBuilder = new StringBuilder();
        for (var argument : arguments)
        {
            if (!arguments.getLast().equals(argument)) {
                argumentsBuilder.append(argument).append(" ");
            }
            else {
                argumentsBuilder.append(argument);
            }
        }
        String argumentText = argumentsBuilder.toString();
        switch (command) {
            case "echo" -> printEcho(argumentText);
            case "type" -> printCommandType(argumentText);
            case "pwd" -> printWorkingDirectory();
            case "cd" -> changeDirectory(argumentText);
        }
    }
    private static void printEcho(String argumentText) {

            System.out.println(argumentText);
    }


    private static void printWorkingDirectory() {
        System.out.println(currentDirectory);


    }
    private static void printCommandType(String commandName)
    {
        if (isBuiltInCommand(commandName)) {
            System.out.println(commandName + " is a shell builtin");
        } else {
            PathUtils.printExecutablePath(commandName);
        }

    }

    private static void changeDirectory(String directoryArgument)
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
        Path absolutePath = Path.of(currentDirectory).resolve(targetPath);
        //ignore the tempPath if it is absolute path
        boolean directoryExists = false;

        try{
            absolutePath = absolutePath.toRealPath();
            directoryExists = true;
        } catch (IOException e) {
            System.err.println("cd: " + directoryArgument + ": No such file or directory");
        }
        if (directoryExists) {
            currentDirectory = absolutePath.toString();
        }
    }
}
