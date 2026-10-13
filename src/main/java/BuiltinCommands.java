import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class BuiltinCommands {

    public final static  String[]listCommands= {"echo","exit","pwd","type","cd"};


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
    public static void runCommands( String command, List<String> userArgument, String fileName, boolean isFileNameProvided)
    {
        StringBuilder argumentBuilder = new StringBuilder();
        for(var i: userArgument)
        {
            argumentBuilder.append(i);

        }

        String argument = argumentBuilder.toString();
        switch (command) {
            case "echo" -> echoFunction(argument,fileName,isFileNameProvided);
            case "type" -> typeFunction(argument,fileName,isFileNameProvided);
            case "pwd" -> pwdFunction(argument,fileName,isFileNameProvided);
            case "cd" ->cdFunction(argument,fileName,isFileNameProvided);
        }
    }
    public static void echoFunction(String userArgument, String fileName, boolean isFileNameProvided) {
        if("no file".equals(fileName)) {
            System.out.println(userArgument);
        }
        else{
            FindPath.createFileforBuiltIN(userArgument, fileName);

        }
    }


    public static void pwdFunction(String userArgument, String fileName, boolean isFileNameProvided) {

        if (isFileNameProvided){
            FindPath.createFileforBuiltIN(userArgument,fileName);
        }
        else {
            System.out.println(path);
        }


    }
    public static void typeFunction(String userArgument, String fileName, boolean isFileNameProvided)
    {
        if (isThisBuiltin(userArgument)) {
            if (isFileNameProvided) {
                FindPath.createFileforBuiltIN(userArgument, fileName);
            } else {
                System.out.println(userArgument + " is a shell builtin");
            }
        }
        else {
            FindPath.filePath(userArgument);
        }
    }

    public static void cdFunction(String userArgument, String fileName, boolean isFileNameProvided)
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
            System.out.println("cd: " +userArgument +": No such file or directory");
        }
        if(isExist){
            path = absolutePath.toString();
        }
        if(isFileNameProvided)
        {
            FindPath.createFileforBuiltIN(userArgument,fileName);
        }
    }
}
