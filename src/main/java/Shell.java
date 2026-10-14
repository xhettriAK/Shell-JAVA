import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {

    record Information(List<String> usrList, String outputfile, String errorfile, boolean missingFileName) {
    }

    public static Information parsing(String userInput) {
        char currentQuote = '\0';
        List<String> usrList = new ArrayList<>();
        StringBuilder userBuilder = new StringBuilder();
        boolean ordinary = false;
        boolean isthereRedirect = false;
        boolean isthereRedirectReverse = false;
        String outputFile = "", errorFile = "";
        for (int i = 0; i < userInput.length(); i++) {
            char c = userInput.charAt(i);
            if ((c == '\\' && !ordinary) && (currentQuote == '\"')) {
                //now check the upcoming char
                if (i != userInput.length() - 1) {
                    char upComing = userInput.charAt(i + 1);
                    if (upComing == '\"') {
                        userBuilder.append(userInput.charAt(i + 1));
                        i++;
                        continue;
                    } else if (upComing == '\\') {
                        userBuilder.append(userInput.charAt(i + 1));
                        i++;
                        continue;
                    }
                }
            }
            if ((c == '\\' && !ordinary) && (currentQuote == '\0')) {
                ordinary = true;
            } else if ((c == '\'' || c == '\"') && !ordinary) {
                currentQuote = switch (currentQuote) {
                    case '\0' -> c;
                    case '\'', '\"' -> {
                        if (c == currentQuote) {
                            yield '\0';
                        } else {
                            userBuilder.append(c);
                            yield currentQuote;
                        }
                    }
                    default -> currentQuote;
                };
            } else if ((c == ' ' || c == '\t') && ordinary) {
                userBuilder.append(c);
                ordinary = false;
            } else if ((c == ' ' || c == '\t') && currentQuote != '\0') {
                userBuilder.append(c);
            } else if (c == ' ' || c == '\t') {
                if (!userBuilder.isEmpty()) {
                    if (isthereRedirect) {
                        outputFile = userBuilder.toString();
                        isthereRedirect = false;
                        userBuilder.delete(0, userBuilder.length());
                    } else if (isthereRedirectReverse) {
                        errorFile =userBuilder.toString();
                        isthereRedirectReverse = false;
                        userBuilder.delete(0, userBuilder.length());
                    }
                    else {
                        usrList.add(userBuilder.toString());
                        userBuilder.delete(0, userBuilder.length());
                    }
                }
            } else {
                if ((c == '2') && currentQuote == '\0' && !ordinary) {
                    if (i < userInput.length() - 1) {
                        char upComing = userInput.charAt(i + 1);
                        if (upComing == '>') {
                            if(userBuilder.isEmpty()) {
                                isthereRedirectReverse = true;
                                i++;
                                continue;
                            }
                        }
                    }
                } else if ((c == '1' && currentQuote == '\0') && !ordinary) {
                    if (i < userInput.length() - 1) {
                        char upComing = userInput.charAt(i + 1);
                        if (upComing == '>') {
                           if(userBuilder.isEmpty()) {
                               isthereRedirect = true;
                               i++;
                               continue;
                           }
                        }
                    }
                } else if ((c == '>' && currentQuote == '\0') && !ordinary) {
                    if(!userBuilder.isEmpty()) {
                        usrList.add(userBuilder.toString());
                        userBuilder.delete(0, userBuilder.length());
                    }
                    isthereRedirect = true;
                    continue;
                }
                userBuilder.append(c);
            }
        }
        if (!userBuilder.isEmpty()) {

            if (isthereRedirect) {
                outputFile = userBuilder.toString();
                isthereRedirect = false;
            } else if (isthereRedirectReverse) {
                errorFile =userBuilder.toString();
                isthereRedirectReverse = false;
            }
            else{
            usrList.add(userBuilder.toString());
            }
        }
        return new Information(usrList, outputFile, errorFile, isthereRedirect || isthereRedirectReverse);

    }

    public static void run(Scanner input) {
        String user;
        while (true) {
            System.out.print("$ ");
            user = input.nextLine();
            if (user.isBlank()) {
                continue;
            }
            if("exit".equals(user))
            {
                break;
            }
            Information l = parsing(user);
            List<String> userList = new ArrayList<>(l.usrList);

            List<String> userArgument = new ArrayList<>();

            String command = userList.getFirst();
            if (l.missingFileName) {
                System.out.println("Expected a string, but found end of the input");
            } else {
                if (BuiltinCommands.isThisBuiltin(command)) {
                    boolean error_produce = false;
                    PrintStream originalOut = System.out;
                    PrintStream originalError = System.err;
                    if (!l.outputfile.isBlank()) {


                        try {
                            System.setOut(new PrintStream(FindPath.getAbsolutePath(l.outputfile).toFile()));

                        } catch (FileNotFoundException e) {
                            System.setOut(originalOut);
                            System.out.println(l.outputfile + ": no file or directory");
                            continue;
                        }
                    }
                     if(!l.errorfile.isBlank())
                    {

                        try {
                            System.setErr(new PrintStream(FindPath.getAbsolutePath(l.errorfile).toFile()));

                        } catch (FileNotFoundException e) {
                            System.setErr(originalError);
                            System.out.println( l.errorfile+ ": no file or directory");
                        }
                    }
                    userArgument = userList.subList(1,userList.toArray().length);
                    BuiltinCommands.runCommands(command,userArgument);
                    System.setOut(originalOut);
                    System.setErr(originalError);
                }
                else{
                    ExternalCommands.runCommands(command,userList, l.outputfile, l.errorfile);
                }
            }
        }
    }
}

