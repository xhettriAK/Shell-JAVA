package Shell;

import Builtins.BuiltInCommands;
import Builtins.Jobs;
import Execution.ExternalCommandRunner;
import Execution.PathUtils;
import Token.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE;

public class CommandShell {
    private  static boolean isthisfirsttimeparser = true;

    public static void run(BufferedReader reader) throws IOException  {
        String userInput="";
        while (true) {
            if(!Jobs.isJobListEmpty()) {
                Jobs.Reap();
            }
            if(isthisfirsttimeparser) {
                userInput="";
                System.out.println("> ");
            }
            else {
                System.out.print("$ ");
            }
            userInput+= new LineReader().readUserInput(reader, isthisfirsttimeparser);
            Parser commandParser = null;
            Lexer lex = null;
            if (userInput.isBlank()) {
                continue;
            }
            if("exit".equals(userInput)) {
                break;
            }try {
                lex = new Lexer(userInput);
                commandParser = new Parser(lex.scanTokens());
            } catch (ParserError e) {
                int length = Math.max(e.getLength(), 1);
                String s = " ".repeat(e.getOffset())+"^".repeat(length);
                System.out.print(s);
                System.err.println(e.getMessage());;
            }
           List<Token.Parser.CommandInformation>commandArguments = commandParser.parse();
            List<String> commandArgumentValues;
            if (lex.isInComplete()) {
                isthisfirsttimeparser = false;
                continue;
            }
            if (commandParser.isIncomplete()) {
                isthisfirsttimeparser = false;
                continue;
            }
            String command = "";
            if(commandArguments.size()==1) {

                command = commandArguments.getFirst().arguments().getFirst();
                commandArgumentValues = commandArguments.getFirst().arguments().subList(1, commandArguments.getFirst
                            ().arguments().toArray().length);
                String outputFile = commandArguments.getFirst().outputFile();
                String errorFile = commandArguments.getFirst().errorFile();
                boolean appendOutput = commandArguments.getFirst().outputType()==TokenType.APPEND_OUT;
                boolean appendError = commandArguments.getFirst().errorType() ==TokenType.APPEND_ERR;
                    if (BuiltInCommands.isBuiltInCommand(command)) {
                        PrintStream originalOut = System.out;
                        PrintStream originalError = System.err;

                        if (!outputFile.isBlank()) {
                            try {
                                System.setOut(createPrintStream(appendOutput, outputFile));

                            } catch (IOException e) {
                                System.setOut(originalOut);
                                System.out.println(outputFile+ ": no file or directory");
                                continue;
                            }
                        }
                        if (errorFile.isBlank()) {

                            try {
                                System.setErr(createPrintStream(appendError, errorFile));

                            } catch (IOException e) {
                                System.setErr(originalError);
                                System.out.println(errorFile + ": no file or directory");
                            }
                        }
                        BuiltInCommands.executeBuiltInCommand(command, commandArgumentValues);
                        System.setOut(originalOut);
                        System.setErr(originalError);
                    } else {
                        boolean runInBackground = commandParser.isRunInBackground();
                        if (runInBackground) {
                            commandArguments.remove(commandArguments.toArray().length - 1);
                        }
                        ExternalCommandRunner.executeExternalCommand(command, commandArguments.getFirst().arguments(), outputFile, errorFile,
                                appendOutput, appendError, runInBackground);
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

