package Builtins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static Builtins.Pwd.printWorkingDirectory;

public interface BuiltInCommands{
      String[] BUILT_IN_COMMANDS = {"echo", "exit", "pwd", "type", "cd","complete","jobs"};
     static String completeBuiltInCommand(String commandPrefix)
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
     static boolean isBuiltInCommand(String command)
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
            case "echo" -> Echo.printEcho(argumentText);
            case "type" -> Type.printCommandType(argumentText);
            case "pwd" -> Pwd.printWorkingDirectory();
            case "cd" -> CD.changeDirectory(argumentText);
            case "complete"-> Complete.printCompletion(argumentText);
        }
    }
}