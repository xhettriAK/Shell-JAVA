import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BuiltinCommands {

    public final static  String[]listCommands= {"echo","exit","pwd","type","cd",};

    public static String tabCompletionForBuiltin(String user)
    {
        for(var command : BuiltinCommands.listCommands)
        {
            if(command.startsWith(user))
            {
                return command.substring(user.length());
            }
        }
        return null;
    }
    private static String path= Path.of("").toAbsolutePath().toString();

    public static String getCurrentPath()
    {
        return path;

    }
    public static boolean isThisBuiltin(String command )
    {
        for(var check2: listCommands) {
            if(command.equals(check2)) {
                return true;
            }
        }
        return false;
    }
     static void runCommands( String command, List<String> userArgument )
    {
        StringBuilder argumentBuilder = new StringBuilder();
        for(var i : userArgument)
        {
            if(!userArgument.getLast().equals(i)) {
                argumentBuilder.append(i).append(" ");
            }
            else{
                argumentBuilder.append(i);
            }
        }
        String userArg = argumentBuilder.toString();
        switch (command) {
            case "echo" -> echoFunction(userArg);
            case "type" -> typeFunction(userArg);
            case "pwd" -> pwdFunction();
            case "cd" ->cdFunction(userArg);
        }
    }
    private static void echoFunction(String userArgument) {

            System.out.println(userArgument);
    }


    private static void pwdFunction() {
        System.out.println(path);


    }
    private static void typeFunction(String userArgument)
    {
        if (isThisBuiltin(userArgument)) {
            System.out.println(userArgument + " is a shell builtin");
        } else {
            FindPath.filePath(userArgument);
        }

    }

    private static void cdFunction(String userArgument)
    {
        Path tempPath;
        String username = System.getenv("USER");

        if(userArgument.equals("~"+username)) {
            tempPath = Path.of(System.getenv("HOME"));
        }
        else if(userArgument.startsWith("~")){
            tempPath = Path.of( System.getenv("HOME")+userArgument.substring(1));
        }
        else{
            tempPath    = Path.of(userArgument);
        }
        Path absolutePath = Path.of(path).resolve(tempPath);
        //ignore the tempPath if it is absolute path
        boolean isExist = false;

        try{
            absolutePath = absolutePath.toRealPath();
            isExist = true;
        } catch (IOException e) {
            System.err.println("cd: " +userArgument +": No such file or directory");
        }
        if(isExist){
            path = absolutePath.toString();
        }
    }
}
