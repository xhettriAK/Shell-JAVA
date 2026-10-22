package Shell;

import Builtins.BuiltInCommands;
import Builtins.Jobs;
import Execution.ExternalCommandRunner;
import Execution.PathUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;

public class CommandShell {

    private record CommandInfo(List<String> commandArguments, String outputFile,
                               String errorFile, boolean missingFileName,
                               boolean appendError, boolean appendOutput) {
    }


    public static CommandInfo parseCommandLine(String userInput) {

        List<String> usrList = new ArrayList<>();
        StringBuilder userBuilder = new StringBuilder();
        char currentQuote = '\0';
        boolean ordinary = false;
        boolean isthereRedirect = false;
        boolean isthereRedirectReverse = false;
        boolean appendError = false;
        boolean appendOutput = false;

        String outputFile = "", errorFile = "";

        for (int i = 0; i < userInput.length(); i++) {
            char c = userInput.charAt(i);
            char next = i + 1 < userInput.length() ? userInput.charAt(i + 1) : '\0';

            // Inside double quotes, only escape a quote or another backslash.
            if (c == '\\' && !ordinary && currentQuote == '"' && (next == '"' || next == '\\')) {
                userBuilder.append(next);
                i++;
                continue;
            }
            // when  c is \\ for the first time
            if (c == '\\' && !ordinary && currentQuote == '\0') {
                ordinary = true;
                continue;
            }
            // c is \' or c== '"' and ordinary is false
            if ((c == '\'' || c == '"') && !ordinary) {
                if (currentQuote == '\0') {
                    currentQuote = c;
                } else if (currentQuote == c) {
                    currentQuote = '\0';
                } else {
                    userBuilder.append(c);
                }
                continue;
            }
            // it will put string to list depending whether isthereredirect
            // is there reverse
            if (c == ' ' || c == '\t') {
                if (ordinary || currentQuote != '\0') {
                    userBuilder.append(c);
                    ordinary = false;
                } else if (!userBuilder.isEmpty()) {
                    if (isthereRedirect) {
                        outputFile = userBuilder.toString();
                        isthereRedirect = false;
                    } else if (isthereRedirectReverse) {
                        errorFile = userBuilder.toString();
                        isthereRedirectReverse = false;
                    } else {
                        usrList.add(userBuilder.toString());
                    }
                    userBuilder.setLength(0);
                }
                continue;
            }
            // check only when there is no backslash
            if (currentQuote == '\0' && !ordinary) {
                if ((c == '1' || c == '2') && next == '>' && userBuilder.isEmpty()) {
                    if (c == '1') {
                        isthereRedirect = true;
                        if(i+2<userInput.length()&& userInput.charAt(i+2)=='>')
                        {
                            appendOutput=true;
                            i++;
                        }
                        else{
                            appendOutput=false;
                        }
                    } else {
                        isthereRedirectReverse = true;
                        if(i+2<userInput.length() && userInput.charAt(i+2)=='>')
                        {
                            appendError=true;
                            i++;
                        }
                        else{
                            appendError=false;
                        }
                    }
                    i++;
                    continue;
                }

                if (c == '>') {

                    if (next == '>') {
                        appendOutput = true;
                        i++;
                    }
                    else{
                        appendOutput=false;
                    }
                    if (!userBuilder.isEmpty()) {
                        if (isthereRedirect) {
                            outputFile = userBuilder.toString();
                            isthereRedirect = false;
                        } else if (isthereRedirectReverse) {
                            errorFile = userBuilder.toString();
                            isthereRedirectReverse = false;
                        } else {
                            usrList.add(userBuilder.toString());
                        }
                        userBuilder.setLength(0);
                    }

                    isthereRedirect = true;
                    continue;
                }
            }
            userBuilder.append(c);
            ordinary = false;
        }

        if (!userBuilder.isEmpty()) {
            if (isthereRedirect) {
                outputFile = userBuilder.toString();
                isthereRedirect = false;
            } else if (isthereRedirectReverse) {
                errorFile = userBuilder.toString();
                isthereRedirectReverse = false;
            } else {
                usrList.add(userBuilder.toString());
            }
        }

        return new CommandInfo(
                usrList,
                outputFile,
                errorFile,
                isthereRedirect || isthereRedirectReverse,
                appendError,
                appendOutput
        );
    }

    public static void run(BufferedReader reader) throws IOException  {
        String userInput="";

        while (true) {
            if(!Jobs.isJobListEmpty())
            {
                Jobs.Reap();
            }
            System.out.print("$ ");
            userInput = new LineReader().readUserInput(reader);
            if (userInput.isBlank()) {
                continue;
            }
            if("exit".equals(userInput))
            {
                break;
            }
            CommandInfo commandInfo = parseCommandLine(userInput);
            List<String> commandArguments = new ArrayList<>(commandInfo.commandArguments);
            List<String> commandArgumentValues;

            String command = commandArguments.getFirst();
            if (commandInfo.missingFileName) {
                System.out.println("Expected a string, but found end of the input");
            } else {
                if (BuiltInCommands.isBuiltInCommand(command)) {
                    PrintStream originalOut = System.out;
                    PrintStream originalError = System.err;
                    if (!commandInfo.outputFile.isBlank()) {
                        try {

                            System.setOut(createPrintStream(commandInfo.appendOutput, commandInfo.outputFile));

                        } catch (IOException e) {
                            System.setOut(originalOut);
                            System.out.println(commandInfo.outputFile + ": no file or directory");
                            continue;
                        }
                    }
                    if (!commandInfo.errorFile.isBlank())
                    {

                        try {
                            System.setErr(createPrintStream(commandInfo.appendError, commandInfo.errorFile));

                        } catch (IOException e) {
                            System.setErr(originalError);
                            System.out.println(commandInfo.errorFile + ": no file or directory");
                        }
                    }
                    commandArgumentValues = commandArguments.subList(1, commandArguments.toArray().length);
                    BuiltInCommands.executeBuiltInCommand(command, commandArgumentValues);
                    System.setOut(originalOut);
                    System.setErr(originalError);
                }
                else{
                    boolean runInBackground= commandArguments.getLast().equals("&");
                    if(runInBackground)
                    {
                        commandArguments.remove(commandArguments.toArray().length-1);
                    }
                    ExternalCommandRunner.executeExternalCommand(command, commandArguments, commandInfo.outputFile, commandInfo.errorFile, commandInfo.appendOutput, commandInfo.appendError,runInBackground);
                }
            }

        }
    }
    public static PrintStream createPrintStream(boolean appendOutput, String fileName) throws IOException {
        if (appendOutput) {
            return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName), CREATE, StandardOpenOption.APPEND));
        }
        return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName)));
    }

}

