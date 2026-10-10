import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BuiltinCommands {

    public final static  String[]listCommands= {"echo","exit","pwd","type","cd"};
    
    private static String path= Path.of("").toAbsolutePath().toString();
    public static boolean isThisBuiltin(String command )
    {
        for(var check2: listCommands) {
            if(command.equals(check2)) {
                return true;
            }
        }
        return false;
    }
    public static void runCommands( String command, List<String> userArgument)
    {
        StringBuilder argumentBuilder = new StringBuilder();
        for(var i: userArgument)
        {
            argumentBuilder.append(i);

        }
        String argument = argumentBuilder.toString();
        switch (command) {
            case "echo" -> echoFunction(userArgument);
            case "type" -> typeFunction(argument);
            case "pwd" -> pwdFunction();
            case "cd" ->cdFunction(argument);
        }
    }
    public static void echoFunction(List<String> userArgument)
    {
        for(int i = 0; i<userArgument.toArray().length;i++)
        {
            System.out.print(userArgument.get(i)+" ");
        }
        System.out.println();

//        if(insideQuote)
//        {
//            StringBuilder userArgumentBuilder2 = new StringBuilder(userArgumentBuilder.toString());
//            Scanner n = new Scanner(System.in);
//            String temp = "";
//            while (!temp.endsWith(String.valueOf(checker))) {
//                temp = n.nextLine();
//                userArgumentBuilder.append("\n").append(temp);
//
//            }
//
//        }
    }


    public static void pwdFunction()
    {
        System.out.println(path);
    }
    public static void typeFunction( String argument)
    {
        if (isThisBuiltin(argument)) {
            System.out.println(argument + " is a shell builtin");
        } else {
            FindPath.filePath(argument);
        }
    }
    public static void cdFunction(String userArgument)
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
        boolean isExist = false;

        try{
            absolutePath = absolutePath.toRealPath();
            isExist = true;
        } catch (IOException e) {
            System.out.println("cd: " +userArgument +": No such file or directory");
        }
        if(isExist){
            path = absolutePath.toString();
        }
    }
}
