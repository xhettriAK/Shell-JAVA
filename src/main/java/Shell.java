import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Shell {

    public static List<String> parsing(String userInput)
    {
        boolean insideQuote=false;
        char quoteSame = ' ';
        List<String>usrList = new ArrayList<>();
        StringBuilder userBuilder = new StringBuilder();
        boolean insideQuoteFirstTime=true;
        for(int i = 0;i<userInput.length();i++)
        {
            char c = userInput.charAt(i);
            if(c== '\''|| c=='\"') {
                if(c!=quoteSame&& insideQuoteFirstTime)
                {
                    quoteSame=c;
                    insideQuoteFirstTime=false;
                    insideQuote = true;

                }
                else if(c == quoteSame){
                    insideQuote = false;
                }
                else if(!insideQuote){
                    insideQuoteFirstTime=true;
                }
                else{
                    userBuilder.append(c);
                }
            }
            else if ((c == ' ' || c=='\t')&& insideQuote) {
                userBuilder.append(c);
            }
            else if(c == ' '||c=='\t') {
                if(!userBuilder.isEmpty()) {
                    usrList.add(userBuilder.toString());

                    userBuilder.delete(0,userBuilder.length());
                }

            }
            else{
                userBuilder.append(c);
            }
        }
        if(!userBuilder.isEmpty()) {
            usrList.add(userBuilder.toString());
        }
       return usrList ;

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
            List<String> userList = new ArrayList<>(parsing(user));
            System.out.println(userList.toString());
            if (userList.getFirst().equals("exit")) {
                break;
            }
            String command = userList.getFirst();
            List<String> userArgument = new ArrayList<>(userList.subList(1, userList.toArray().length));

            if (BuiltinCommands.isThisBuiltin(command)) {
                BuiltinCommands.runCommands(command, userArgument);
            } else {
                ExternalCommands.runCommands(command, userList);
            }
        }
    }
}
