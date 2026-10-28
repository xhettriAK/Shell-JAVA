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
    private  static boolean isThisFirstTimeParser = true;

    public static void run(BufferedReader reader) throws ParserError,IOException  {
        String userInput="";
        while (true) {
            if(!Jobs.isJobListEmpty()) {
                Jobs.Reap();
            }
            if(isThisFirstTimeParser) {
                userInput="";
                System.out.print("$ ");
            }
            else {
                System.out.print("> ");
            }
            userInput+= new LineReader().readUserInput(reader, isThisFirstTimeParser);

            Lexer lex = new Lexer(userInput);
            Parser commandParser = new Parser(lex.scanTokens());
            List<Parser.CommandInformation>commandRecord= new ArrayList<>();
            if (userInput.isBlank()) {
                continue;
            }
            if("exit".equals(userInput)) {
                break;
            }try {
                commandRecord = commandParser.parse();
            } catch (ParserError e) {
                int length = Math.max(e.getLength(), 1);
                String s = " ".repeat(e.getOffset())+"^".repeat(length);
                System.err.println("shell: "+e.getMessage());
                System.err.println(userInput);
                System.err.println(s);
            }

            List<String> commandArgumentValues;
            if (lex.isInComplete()||commandParser.isIncomplete()) {
                isThisFirstTimeParser = false;
                continue;
            }
            String command = "";
            if(commandRecord.size()==1) {
                command = commandRecord.getFirst().arguments().getFirst();
                List<String> commandArguments = commandRecord.getFirst().arguments();
                commandArgumentValues = commandArguments.subList(1,commandArguments.toArray().length);

                String outputFile = commandRecord.getFirst().outputFile();
                String errorFile = commandRecord.getFirst().errorFile();
                boolean appendOutput = commandRecord.getFirst().outputType()==TokenType.APPEND_OUT;
                boolean appendError = commandRecord.getFirst().errorType() ==TokenType.APPEND_ERR;

                if (BuiltInCommands.isBuiltInCommand(command)) {
                    PrintStream originalOut = System.out;
                    PrintStream originalError = System.err;

                    if (!outputFile.isBlank()) {
                        try {
                            System.setOut(createPrintStream(appendOutput, outputFile));

                        } catch (IOException e) {
                            System.setOut(originalOut);
                            System.out.println(outputFile + ": no file or directory");
                            continue;
                        }
                    }
                    if (!errorFile.isBlank()) {
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

                    ExternalCommandRunner.executeExternalCommand(commandArguments, outputFile, errorFile,
                            appendOutput, appendError, runInBackground);
                }
            }
            isThisFirstTimeParser = true;
        }
    }


    public static PrintStream createPrintStream(boolean appendOutput, String fileName) throws IOException {
        if (appendOutput) {
            return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName), CREATE, StandardOpenOption.APPEND));
        }
        return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName)));
    }

}

