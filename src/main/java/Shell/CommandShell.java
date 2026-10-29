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
    // false while the previous line was incomplete and we are reading its continuation
    private static boolean startingNewCommand = true;

    public static void run(BufferedReader reader) throws ParserError, IOException {
        String userInput = "";
        while (true) {
            if (!Jobs.isJobListEmpty()) {
                Jobs.Reap();
            }
            if (startingNewCommand) {
                userInput = "";
                System.out.print("$ ");
            } else {
                System.out.print("> ");
            }
            userInput += new LineReader().readUserInput(reader, startingNewCommand);

            Lexer lexer = new Lexer(userInput);
            Parser parser = new Parser(lexer.scanTokens());
            List<Parser.CommandInformation> pipeline = new ArrayList<>();
            if (userInput.isBlank()) {
                continue;
            }
            if ("exit".equals(userInput)) {
                break;
            }
            try {
                pipeline = parser.parse();
            } catch (ParserError e) {
                int length = Math.max(e.getLength(), 1);
                String errorMarker = " ".repeat(e.getOffset()) + "^".repeat(length);
                System.err.println("shell: " + e.getMessage());
                System.err.println(userInput);
                System.err.println(errorMarker);
            }
            if (lexer.isInComplete() || parser.isIncomplete()) {
                startingNewCommand = false;
                continue;
            }
            if (pipeline.size() == 1) {
                Parser.CommandInformation commandInfo = pipeline.getFirst();
                List<String> arguments = commandInfo.arguments();
                String command = arguments.getFirst();
                List<String> commandArguments = arguments.subList(1, arguments.size());

                String outputFile = commandInfo.outputFile();
                String errorFile = commandInfo.errorFile();
                boolean appendOutput = commandInfo.outputType() == TokenType.APPEND_OUT;
                boolean appendError = commandInfo.errorType() == TokenType.APPEND_ERR;

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
                    BuiltInCommands.executeBuiltInCommand(command, commandArguments);
                    System.setOut(originalOut);
                    System.setErr(originalError);
                } else {
                    boolean runInBackground = parser.isRunInBackground();

                    ExternalCommandRunner.executeExternalCommand(arguments, outputFile, errorFile,
                            appendOutput, appendError, runInBackground);
                }
            } else {
                String outputFile = "";
                boolean appendOutput = false;
                boolean pipelineInvalid = false;

                for (int i = 0; i < pipeline.size(); i++) {
                    String command = pipeline.get(i).arguments().getFirst();
                    if (command.isBlank() || (!BuiltInCommands.isBuiltInCommand(command) && ExternalCommandRunner.isCommandNotAvailable(command))) {
                        System.err.println("shell: Unknown command " + command);
                        System.err.println(userInput);
                        System.err.println();
                        pipelineInvalid = true;
                    }
                    if (i < pipeline.size() - 1 && !pipeline.get(i).outputFile().isBlank()) {
                        System.err.println("shell: Unsupported :(");
                        //java doesnt support this feature
                        System.err.println(userInput);
                        pipelineInvalid = true;
                    } else if (i == pipeline.size() - 1 && !pipeline.get(i).outputFile().isBlank()) {
                        outputFile = pipeline.getLast().outputFile();
                        appendOutput = pipeline.getLast().outputType() == TokenType.APPEND_OUT;
                    }
                }
                if (!pipelineInvalid) {
                    String command;
                    List<String> arguments;
                    List<String> commandArguments;
                    // consecutive external commands waiting to be run as one process pipe
                    List<Parser.CommandInformation> pendingExternalCommands = new ArrayList<>();
                    ByteArrayOutputStream builtinOutput = null;
                    boolean isLastCommand = false;

                    for (int i = 0; i < pipeline.size(); i++) {
                        arguments = pipeline.get(i).arguments();
                        command = arguments.getFirst();
                        commandArguments = arguments.subList(1, arguments.size());
                        PrintStream originalOut = System.out;
                        boolean builtinWroteToBuffer = false;
                        if (i == pipeline.size() - 1) {
                            isLastCommand = true;
                        }
                        if (BuiltInCommands.isBuiltInCommand(command)) {
                            if (!pendingExternalCommands.isEmpty()) {
                                ExternalCommandRunner.executeCommandPipe(pendingExternalCommands, builtinOutput, false, outputFile, appendOutput);
                                if (builtinOutput != null) {
                                    builtinOutput.reset();
                                }
                                pendingExternalCommands.clear();
                            }
                            if (isLastCommand) {
                                if (!outputFile.isBlank()) {
                                    try {
                                        System.setOut(createPrintStream(appendOutput, outputFile));

                                    } catch (IOException e) {
                                        System.setOut(originalOut);
                                        System.out.println(outputFile + ": no file or directory");
                                        continue;
                                    }
                                } else {

                                    BuiltInCommands.executeBuiltInCommand(command, commandArguments);
                                }
                                System.setOut(originalOut);
                            } else {
                                builtinOutput = new ByteArrayOutputStream();
                                PrintStream bufferStream = new PrintStream(builtinOutput);
                                System.setOut(bufferStream);
                                BuiltInCommands.executeBuiltInCommand(command, commandArguments);
                                builtinWroteToBuffer = true;
                                System.setOut(originalOut);
                            }
                        } else {
                            pendingExternalCommands.add(pipeline.get(i));
                        }
                        if (!pendingExternalCommands.isEmpty() && (builtinWroteToBuffer || isLastCommand)) {
                            ExternalCommandRunner.executeCommandPipe(pendingExternalCommands, builtinOutput, isLastCommand, outputFile, appendOutput);
                            if (builtinOutput != null) {
                                builtinOutput.reset();
                            }
                            pendingExternalCommands.clear();
                        }
                    }
                }
            }
            startingNewCommand = true;
        }
    }

    public static PrintStream createPrintStream(boolean appendOutput, String fileName) throws IOException {
        if (appendOutput) {
            return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName), CREATE, StandardOpenOption.APPEND));
        }
        return new PrintStream(Files.newOutputStream(PathUtils.resolveFromCurrentDirectory(fileName)));
    }

}
