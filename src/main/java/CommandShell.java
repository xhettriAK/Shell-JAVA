import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;

public class CommandShell {

    private record CommandInfo(List<String> commandArguments, String outputFile,
                               String errorFile, boolean missingFileName,
                               boolean appendError, boolean appendOutput) {
    }
    private static boolean previousKeyWasTab = false;

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

    public static String findCommonPrefix(List<String> user)
    //list is sorted| Comparing first and last
    {
        StringBuilder prefix= new StringBuilder();
        String firstWord = user.getFirst();
        String lastWord = user.getLast();
        for(int i=0; i<firstWord.length();i++)
        {
            if(firstWord.charAt(i)!=lastWord.charAt(i)) {
                    return prefix.toString();
            }
            else {
                    prefix.append(lastWord.charAt(i));
            }
        }
        return prefix.toString();
    }
    public static String completeFileName(String filePrefix, Set<String> matches, String input)
    {
        String temp = "";
        if (matches.size() == 1) {
            for (var match : matches) {
                temp = match.substring(filePrefix.length())+' ';
                System.out.print(temp);
            }
        } else {
            System.out.println();
            for (var match : matches) {
                System.out.print(match + " ");
            }
            System.out.println();
            System.out.print("$ " + input);
            return "";
        }
        return temp;

    }
    public static String completeExternalCommand(StringBuilder inputBuilder, Set<String> matches)
    {
        String temp ="";
        if (matches.size() == 1) {
            for (var match : matches) {
                temp = match.substring(inputBuilder.toString().length())+" ";
                System.out.print(temp);
            }
        } else {
            if (previousKeyWasTab) {


                System.out.println();
                for (var match : matches) {
                    System.out.print(match + " ");
                }
                System.out.println();
                System.out.print("$ " + inputBuilder);
            }
            return "";
        }
        return temp;

    }
    public static String readUserInput(BufferedReader reader) throws IOException
    {
        char inputCharacter;
        int inputCode;
        StringBuilder inputBuilder = new StringBuilder();
        ValueRange printableCharacters = ValueRange.of(32, 126);

        while (((inputCode = reader.read()) != -1)) {

            inputCharacter = (char) inputCode;

            if (printableCharacters.isValidValue(inputCode)) {
                previousKeyWasTab = false;
                System.out.print(inputCharacter);
                inputBuilder.append(inputCharacter);

            } else {
                if (inputCharacter == '\u0008' || inputCharacter == 127)
                {
                    if (!inputBuilder.isEmpty()) {

                        inputBuilder.deleteCharAt(inputBuilder.length() - 1);
                        System.out.print("\b \b");

                    }
                    previousKeyWasTab = false;
                }
                if (inputCharacter == '\n') {
                    previousKeyWasTab = false;
                    System.out.println();
                    break;
                }
                if (inputCharacter == '\t' && inputBuilder.isEmpty()) {
                    //tab{if(userBuilder.isEmpty())
                    inputBuilder.append(inputCharacter);
                }
                else if (inputCharacter == '\t' && inputBuilder.toString().contains(" ")) {
                    String filePrefix = inputBuilder.substring(inputBuilder.lastIndexOf(" ") + 1);
                    Set<String> matches = PathUtils.findFileCompletions(filePrefix);
                    if (!matches.isEmpty()) {
                        String prefix = "";
                        if (matches.size() > 1) {
                            prefix = findCommonPrefix(new ArrayList<>(matches));
                        }
                        if ((prefix.isEmpty() || filePrefix.equals(prefix))) {
                            inputBuilder.append(completeFileName(filePrefix, matches, inputBuilder.toString()));
                        } else {
                            String completion = prefix.substring(filePrefix.length());
                            inputBuilder.append(completion);
                            System.out.print(completion);
                        }
                    } else {
                        System.out.print("\u0007");
                    }
                }
                else if (inputCharacter == '\t') {
                    String completion = BuiltInCommands.completeBuiltInCommand(inputBuilder.toString());
                    if (completion != null) {
                        inputBuilder.append(completion).append(' ');
                        System.out.print(completion + ' ');
                    } else {
                        Set<String> matches = ExternalCommandRunner.findExternalCommandCompletions(inputBuilder.toString());

                        if (!matches.isEmpty()) {
                            String prefix = "";
                            if (matches.size() > 1) {
                                prefix = findCommonPrefix(new ArrayList<>(matches));
                                if (!prefix.isEmpty())
                                {
                                    if (inputBuilder.toString().equals(prefix) && !previousKeyWasTab)
                                    {
                                        System.out.print("\u0007");
                                        previousKeyWasTab = true;
                                    }
                                }
                            }
                            if ((prefix.isEmpty() || inputBuilder.toString().equals(prefix))) {

                                inputBuilder.append(completeExternalCommand(inputBuilder, matches));
                            } else {
                                String completionText = prefix.substring(inputBuilder.toString().length());
                                inputBuilder.append(completionText);
                                System.out.print(completionText);
                                System.out.print("\u0007");
                            }
                        }
                    }

                } else{
                    System.out.print("");
                }
            }
        }

        return inputBuilder.toString();
    }
    public static void run(BufferedReader reader) throws IOException  {
        String userInput="";

        while (true) {
            System.out.print("$ ");
            userInput = readUserInput(reader);
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
                    ExternalCommandRunner.executeExternalCommand(command, commandArguments, commandInfo.outputFile, commandInfo.errorFile, commandInfo.appendOutput, commandInfo.appendError);
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

