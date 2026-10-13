import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {

    record Information(List<String>usrList, int redirectIndex){};

    public static Information parsing(String userInput)
    {
        char currentQuote = '\0';
        List<String>usrList = new ArrayList<>();
        StringBuilder userBuilder = new StringBuilder();
        boolean ordinary = false;
        int boxesCount = -1;
        for(int i = 0;i<userInput.length();i++) {
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
                    usrList.add(userBuilder.toString());
                    userBuilder.delete(0, userBuilder.length());
                }
            } else {
                if ((c == '1' && currentQuote == '\0') && !ordinary) {
                    if (i < userInput.length() - 1) {
                        char upComing = userInput.charAt(i + 1);
                        if (upComing == '>') {
                            if (!userBuilder.isEmpty()) {
                                usrList.add(userBuilder.toString());
                                userBuilder.delete(0, userBuilder.length());
                            }
                                usrList.add(">");
                                boxesCount = usrList.toArray().length - 1;
                                i++;
                                continue;
                        }
                    }
                } else if ((c == '>' && currentQuote == '\0') && !ordinary) {
                    if (!userBuilder.isEmpty()) {
                        usrList.add(userBuilder.toString());
                        userBuilder.delete(0, userBuilder.length());
                    }
                    usrList.add(">");
                    boxesCount = usrList.toArray().length - 1;
                    continue;
                }
                userBuilder.append(c);
                ordinary = false;
            }
        }
        if(!userBuilder.isEmpty()) {
                usrList.add(userBuilder.toString());
        }
        return  new Information(usrList, boxesCount);

    }
    public static void run( Scanner input)
    {
        String user;
        while (true) {
            System.out.print("$ ");
            user = input.nextLine();
            if(user.isEmpty())
            {
                continue;
            }
            Information l = parsing(user);
            List<String> userList = new ArrayList<>(l.usrList);
            String fileName = "no file";
            List<String> userArgument=new ArrayList<>();
            int redirectUnquoted = l.redirectIndex;

            if(redirectUnquoted!=-1 && redirectUnquoted==userList.toArray().length-1)
            {
                System.out.println("Expected a string, but found end of the input");
            }
            else if( redirectUnquoted!=-1) {
                fileName = userList.get(redirectUnquoted + 1);
                userArgument = userList.subList(1,redirectUnquoted);
                userList = userList.subList(0,redirectUnquoted);
            }
            else {
                userArgument = userList.subList(1, userList.toArray().length);
            }
            if (userList.getFirst().equals("exit")) {
                break;
            }
            String command = userList.getFirst();
            boolean isFileNameProvided = !("no file".equals(fileName));
            if (BuiltinCommands.isThisBuiltin(command)) {
                    BuiltinCommands.runCommands(command, userArgument,fileName,isFileNameProvided);
            } else {
                ExternalCommands.runCommands(command, userList, fileName, isFileNameProvided);
            }
        }
    }
}
