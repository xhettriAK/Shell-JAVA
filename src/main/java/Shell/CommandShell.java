package Shell;

import Builtins.BuiltInCommands;
import Builtins.Jobs;
import Execution.ExternalCommandRunner;
import Execution.PathUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;

public class CommandShell {
    private  static boolean isthisfirsttimeparser = true;

    public static void run(BufferedReader reader) throws IOException  {
        String userInput="";
        while (true) {
            if(isthisfirsttimeparser) {
                userInput="";
            }
            if(!Jobs.isJobListEmpty()) {
                Jobs.Reap();
            }
            System.out.print("$ ");
            userInput+= new LineReader().readUserInput(reader, isthisfirsttimeparser);
            if (userInput.isBlank()) {
                continue;
            }
            if("exit".equals(userInput)) {
                break;
            }
            Shell.CommandParser Command = new CommandParser();
            List<String> commandArguments = Command.parseCommandLine(userInput,isthisfirsttimeparser);
            List<String> commandArgumentValues=commandArguments.subList(1, commandArguments.toArray().length);

            if(Command.getCurrentQuote()!='\0') {
                isthisfirsttimeparser =false;
                continue;
            }
            if(Command.getPipeStatus()==1 && Command.getLastChar()=='|') {
                isthisfirsttimeparser = false;
                continue;
            }
            if(Command.getPipeStatus()==-1|| commandArguments.getFirst().equals("|")) {
                System.out.println("Expected a string, but found a pipe");
                isthisfirsttimeparser =true;
                continue;
            }

            String command = commandArguments.getFirst();
            if (Command.isAppendError()) {
                System.out.println("Expected a string, but found end of the input");
            } else {
                if (BuiltInCommands.isBuiltInCommand(command)) {
                    PrintStream originalOut = System.out;
                    PrintStream originalError = System.err;
                    if (!Command.getOutputFile().isBlank()) {
                        try {

                            System.setOut(createPrintStream(Command.isAppendOutput(), Command.getOutputFile()));

                        } catch (IOException e) {
                            System.setOut(originalOut);
                            System.out.println(Command.getOutputFile() + ": no file or directory");
                            continue;
                        }
                    }
                    if (!Command.getErrorFile().isBlank()) {

                        try {
                            System.setErr(createPrintStream(Command.isAppendError(), Command.getErrorFile()));

                        } catch (IOException e) {
                            System.setErr(originalError);
                            System.out.println(Command.getErrorFile() + ": no file or directory");
                        }
                    }
                    BuiltInCommands.executeBuiltInCommand(command, commandArgumentValues);
                    System.setOut(originalOut);
                    System.setErr(originalError);
                } else {
                    boolean runInBackground = commandArguments.getLast().equals("&");
                    if(Command.pipeStatus==1)
                    {

                    }
                    if (runInBackground) {
                        commandArguments.remove(commandArguments.toArray().length - 1);
                    }
                    ExternalCommandRunner.executeExternalCommand(command, commandArguments, Command.getOutputFile(), Command.getErrorFile(),
                            Command.isAppendOutput(), Command.isAppendError(), runInBackground, Command.getPipeStatus());
                }
            }
            isthisfirsttimeparser = true;
        }

    }
    public static PrintStream createPrintStream(boolean appendOutput, String fileName) throws IOException {
        if (appendOutput) {
            return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName), CREATE, StandardOpenOption.APPEND));
        }
        return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName)));
    }

}

