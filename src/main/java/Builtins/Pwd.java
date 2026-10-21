package Builtins;

public class Pwd implements BuiltInCommands {
     static void printWorkingDirectory() {
        System.out.println(WorkingDirectory.getCurrentDirectory());
    }
}

