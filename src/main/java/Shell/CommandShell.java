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
            if (lex.isInComplete()||commandParser.isIncomplete()) {
                isThisFirstTimeParser = false;
                continue;
            }
            if(commandRecord.size()==1) {
               String command = commandRecord.getFirst().arguments().getFirst();
                List<String> commandArguments = commandRecord.getFirst().arguments();
                List<String>commandArgumentValues = commandArguments.subList(1,commandArguments.toArray().length);

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
            }else{
                String outputFile = "";
                boolean appendOutput = false;
                boolean unsupported = false;

                for(int i = 0;i<commandRecord.size(); i++) {
                    String command = commandRecord.get(i).arguments().getFirst();
                    if(command.isBlank()||(!BuiltInCommands.isBuiltInCommand(command) && ExternalCommandRunner.isCommandAvailable(command))){
                        System.err.println("shell: Unknown command " +command);
                        System.err.println(userInput);
                        System.err.println();
                        unsupported = true;
                    }
                    if(i<commandRecord.size()-1 && !commandRecord.get(i).outputFile().isBlank()){
                        System.err.println("shell: Unsupported :(");
                        //java doesnt support this feature
                        System.err.println(userInput);
                        unsupported =true;
                    }else if(i==commandRecord.size()-1 && !commandRecord.get(i).outputFile().isBlank())
                    {
                        outputFile = commandRecord.getLast().outputFile();
                        appendOutput = commandRecord.getLast().outputType()==TokenType.APPEND_OUT;
                    }
                }
                if(!unsupported){
                    String command;
                    List<String> argument;
                    List<String>commandArguments;
                    List<Parser.CommandInformation>External= new ArrayList<>();
                    ByteArrayOutputStream memoryBuffer = null;
                    boolean isThisLastCommand = false;

                    for (int i = 0; i< commandRecord.size();i++) {
                        commandArguments = commandRecord.get(i).arguments();
                        command = commandArguments.getFirst();
                        argument = commandArguments.subList(1, commandArguments.toArray().length);
                        PrintStream originalOut = System.out;
                        boolean isCommandAfterExternalisBuiltin= false;
                        if(i==commandRecord.size()-1){
                            isThisLastCommand = true;
                        }
                        if (BuiltInCommands.isBuiltInCommand(command)) {
                            if(!External.isEmpty()) {
                                ExternalCommandRunner.executeCommandPipe(External, memoryBuffer, false,outputFile,appendOutput);
                                if (memoryBuffer != null) {
                                    memoryBuffer.reset();
                                }
                                External.clear();
                            }
                            if(isThisLastCommand){
                                BuiltInCommands.executeBuiltInCommand(command, argument);
                            }else {
                                memoryBuffer = new ByteArrayOutputStream();
                                PrintStream customStream = new PrintStream(memoryBuffer);
                                System.setOut(customStream);
                                BuiltInCommands.executeBuiltInCommand(command, argument);
                                isCommandAfterExternalisBuiltin = true;
                                System.setOut(originalOut);
                            }
                        } else {
                            External.add(commandRecord.get(i));
                        }
                        if(!External.isEmpty() && (isCommandAfterExternalisBuiltin || isThisLastCommand)) {
                            ExternalCommandRunner.executeCommandPipe(External, memoryBuffer, isThisLastCommand,outputFile,appendOutput);
                            if (memoryBuffer != null) {
                                memoryBuffer.reset();
                            }
                            External.clear();
                        }
                    }
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

