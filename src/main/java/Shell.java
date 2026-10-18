import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.nio.file.StandardOpenOption.CREATE;

public class Shell {

    private record Information(List<String> usrList, String outputFile,
                               String errorFile, boolean missingFileName,
                               boolean appendError, boolean appendOutput) {
    }
    private static boolean  isPreviousKeyTab = false;

    public static Information parsing(String userInput) {

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

        return new Information(
                usrList,
                outputFile,
                errorFile,
                isthereRedirect || isthereRedirectReverse,
                appendError,
                appendOutput
        );
    }

    public static String findPrefix(List<String>user)
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
    public static String tabCompleteFile(String  filePrefix, Set<String>list, String file)
    {
        String temp = "";
        if (list.size() == 1) {
            for (var i : list) {
                temp = i.substring(filePrefix.length())+' ';
                System.out.print(temp);
            }
        } else {
            System.out.println();
            for (var i : list) {
                System.out.print(i + " ");
            }
            System.out.println();
            System.out.print("$ " + file);
            return "";
        }
        return temp;

    }
    public static String tabCompleteExternal(StringBuilder userBuilder, Set<String>list)
    {
        String temp ="";
        if (list.size() == 1) {
            for (var i : list) {
                temp = i.substring(userBuilder.toString().length())+" ";
                System.out.print(temp);
            }
        } else {
            if(isPreviousKeyTab) {


                System.out.println();
                for (var i : list) {
                    System.out.print(i + " ");
                }
                System.out.println();
                System.out.print("$ " + userBuilder.toString());
            }
            return "";
        }
        return temp;

    }
    public static String getuserInput(BufferedReader reader )throws  IOException
    {
        char user;
        int data;
        StringBuilder userBuilder = new StringBuilder();
        ValueRange range = ValueRange.of(32, 126);

        while(((data=reader.read())!=-1 )) {

            user = (char) data;

            if (range.isValidValue(data)) {
                isPreviousKeyTab=false;
                System.out.print(user);
                userBuilder.append(user);

            } else {
                if (user == '\u0008' || user == 127)//backsapce
                {
                    if (!userBuilder.isEmpty()) {

                        userBuilder.deleteCharAt(userBuilder.length() - 1);
                        System.out.print("\b \b");

                    }
                    isPreviousKeyTab=false;
                }
                if (user == '\n') {
                    isPreviousKeyTab=false;
                    System.out.println();
                    break;
                }
                if (user == '\t' && userBuilder.isEmpty()) {
                    //tab{if(userBuilder.isEmpty())
                    userBuilder.append(user);
                }
                else if (user == '\t' && userBuilder.toString().contains(" ")) {
                    String filePrefix = userBuilder.substring(userBuilder.lastIndexOf(" ")+1);
                    Set<String> list = FilePath.tabCompletetionForFile(filePrefix);
                    if (!list.isEmpty()) {
                        String prefix = "";
                        if (list.size() > 1) {
                            prefix = findPrefix(new ArrayList<>(list));
                        }
                        if ((prefix.isEmpty() || filePrefix.equals(prefix))) {
                            userBuilder.append(tabCompleteFile(filePrefix, list, userBuilder.toString()));
                        } else {
                            String i = prefix.substring(filePrefix.length());
                            userBuilder.append(i);
                            System.out.print(i);
                        }
                    } else {
                        System.out.print("\u0007");
                    }
                }
                else if (user == '\t') {
                    String temp = BuiltinCommands.tabCompletionForBuiltin(userBuilder.toString());
                    if (temp != null) {
                        userBuilder.append(temp).append(' ');
                        System.out.print(temp + ' ');
                    } else {
                        Set<String> list = ExternalCommands.tabCompletionForExternal(userBuilder.toString());

                        if (!list.isEmpty()) {
                            String prefix = "";
                            if (list.size() > 1) {
                                prefix = findPrefix(new ArrayList<>(list));
                                if(!prefix.isEmpty())
                                {
                                    if(userBuilder.toString().equals(prefix) && !isPreviousKeyTab)
                                    {
                                        System.out.print("\u0007");
                                        isPreviousKeyTab=true;
                                    }
                                }
                            }
                            if ((prefix.isEmpty() || userBuilder.toString().equals(prefix))) {

                                userBuilder.append(tabCompleteExternal(userBuilder, list));
                            } else {
                                String i = prefix.substring(userBuilder.toString().length());
                                userBuilder.append(i);
                                System.out.print(i);
                            }
                        }
                    }

                }  else {
                    System.out.print("\u0007");
                }
            }
        }

        return userBuilder.toString();
    }
    public static void run(BufferedReader reader) throws IOException  {
        String userInput="";

        while (true) {
            System.out.print("$ ");
            userInput =  getuserInput(reader);
            if (userInput.isBlank()) {
                continue;
            }
            if("exit".equals(userInput))
            {
                break;
            }
            Information l = parsing(userInput);
            List<String> userList = new ArrayList<>(l.usrList);
            List<String> userArgument;

            String command = userList.getFirst();
            if (l.missingFileName) {
                System.out.println("Expected a string, but found end of the input");
            } else {
                if (BuiltinCommands.isThisBuiltin(command)) {
                    PrintStream originalOut = System.out;
                    PrintStream originalError = System.err;
                    if (!l.outputFile.isBlank()) {
                        try {

                            System.setOut(createPrintStream(l.appendOutput,l.outputFile));

                        } catch (IOException e) {
                            System.setOut(originalOut);
                            System.out.println(l.outputFile + ": no file or directory");
                            continue;
                        }
                    }
                    if(!l.errorFile.isBlank())
                    {

                        try {
                            System.setErr(createPrintStream(l.appendError, l.errorFile));

                        } catch (IOException e) {
                            System.setErr(originalError);
                            System.out.println( l.errorFile + ": no file or directory");
                        }
                    }
                    userArgument = userList.subList(1,userList.toArray().length);
                    BuiltinCommands.runCommands(command,userArgument);
                    System.setOut(originalOut);
                    System.setErr(originalError);
                }
                else{
                    ExternalCommands.runCommands(command,userList, l.outputFile, l.errorFile, l.appendOutput,l.appendError);
                }
            }
        }
    }
    public static PrintStream createPrintStream (boolean isAppendTrue, String fileName) throws IOException {
        if (isAppendTrue) {
            return new PrintStream(Files.newOutputStream(FilePath.getAbsolutePath(fileName), CREATE, StandardOpenOption.APPEND));
        }
        return new PrintStream(Files.newOutputStream(FilePath.getAbsolutePath(fileName)));
    }

}

