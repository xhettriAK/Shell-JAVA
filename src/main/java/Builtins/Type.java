package Builtins;
import Execution.*;

public class Type  implements BuiltInCommands{
     static void printCommandType(String commandName)
    {
        if (BuiltInCommands.isBuiltInCommand(commandName)) {
            System.out.println(commandName + " is a shell builtin");
        } else {
            PathUtils.printExecutablePath(commandName);
        }

    }

}
