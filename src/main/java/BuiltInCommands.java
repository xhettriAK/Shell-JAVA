import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class BuiltInCommands {

    public final static String[] BUILT_IN_COMMANDS = {"echo", "exit", "pwd", "type", "cd","complete"};
    private static  HashMap<String, String> completorList = new HashMap<>();

    private static String currentDirectory = Path.of("").toAbsolutePath().toString();

    public static String completeBuiltInCommand(String commandPrefix)
    {
        for (var command : BUILT_IN_COMMANDS)
        {
            if (command.startsWith(commandPrefix))
            {
                return command.substring(commandPrefix.length());
            }
        }
        return null;
    }
    public static String getCurrentDirectory()
    {
        return currentDirectory;

    }
    public static boolean isBuiltInCommand(String command)
    {
        for (var builtInCommand : BUILT_IN_COMMANDS) {
            if (command.equals(builtInCommand)) {
                return true;
            }
        }
        return false;
    }
    static void executeBuiltInCommand(String command, List<String> arguments)
    {
        StringBuilder argumentsBuilder = new StringBuilder();
        for (var argument : arguments)
        {
            if (!arguments.getLast().equals(argument)) {
                argumentsBuilder.append(argument).append(" ");
            }
            else {
                argumentsBuilder.append(argument);
            }
        }
        String argumentText = argumentsBuilder.toString();
        switch (command) {
            case "echo" -> printEcho(argumentText);
            case "type" -> printCommandType(argumentText);
            case "pwd" -> printWorkingDirectory();
            case "cd" -> changeDirectory(argumentText);
            case "complete"-> printCompletion(argumentText);
        }
    }
    private static void printCompletion(String argumentText) {
        String[] CompletionFunction = {"-p", "-C"};
        String[] CompletionArguments = argumentText.trim().split(" ");
        String Value ="";

        StringBuilder commandBuilder = new StringBuilder();
        boolean doRest = true;
        int pMask = 1;
        int cMask = 2;
        BitSet flag = new BitSet(8);
        for(int i = 0; i<CompletionArguments.length; i++)
        {
            if(CompletionArguments[i].equals(CompletionFunction[0]))
            {
                flag.set(pMask,true);
            }
            else if(CompletionArguments[i].equals(CompletionFunction[1]))
            {
                flag.set(cMask,true);
                if(i+1<CompletionArguments.length && !CompletionArguments[i+1].equals(" "))
                {
                    Value = CompletionArguments[i+1].trim();
                    i++;
                }
                else{
                    System.out.println("-C requires path as argument");
                    doRest = false;
                    break;
                }
            }
            else
            {
                commandBuilder.append(CompletionArguments[i]);
            }
        }
        String command = commandBuilder.toString();

        if(doRest) {

            if (flag.get(pMask) && flag.get(cMask)) {
                System.out.println("complete cant have both commands");
            } else if (flag.get(pMask)) {
                if (completorList.isEmpty() && command.isEmpty()) {
                    System.out.println();
                }
                else{
                    if(!command.isEmpty() && completorList.get(command)!=null)
                    {
                        System.out.println("complete -C '"+ completorList.get(command)+"' " + command);

                    }
                    else if(!command.isEmpty() && completorList.get(command)==null)
                    {
                        System.out.println("complete: "+ command+": no completion specification");
                    }
                    else{
                        for(String key: completorList.keySet())
                        {
                            System.out.println("complete -C '"+ completorList.get(key)+"' " + key);
                        }
                    }
                }

            }else if(flag.get(cMask))
            {
                completorList.put(command, Value);
            }
            else{
                System.out.println();
            }

        }
        flag.set(cMask,false);
        flag.set(pMask,false);
    }

    public static  boolean commandExistInCompleter(String userInput) {
        return completorList.get(userInput.strip())!=null;
    }
    public static void executeCompletorCommand(StringBuilder userInput)
    {
        String argv1,argv2="", argv3="";
        argv1 = userInput.substring(0,userInput.indexOf(" "));
        if(userInput.toString().strip().length()>1) {
           String[] argvs = userInput.toString().split(" ");
           for(int i = 0; i< argvs.length; i++)
           {
              if(i  == argvs.length - 1)
              {
                  argv2 = argvs[i];
                  argv3 = argvs[i-1];
              }
           }
        }
        else{
            argv3 =argv1;
        }

        ExternalCommandRunner.executeCompletorCommand(completorList.get(argv1),argv2,argv3);
    }
    private static void printEcho(String argumentText) {

            System.out.println(argumentText);
    }


    private static void printWorkingDirectory() {
        System.out.println(currentDirectory);


    }
    private static void printCommandType(String commandName)
    {
        if (isBuiltInCommand(commandName)) {
            System.out.println(commandName + " is a shell builtin");
        } else {
            PathUtils.printExecutablePath(commandName);
        }

    }

    private static void changeDirectory(String directoryArgument)
    {
        Path targetPath;
        String userName = System.getenv("USER");

        if (directoryArgument.equals("~" + userName)) {
            targetPath = Path.of(System.getenv("HOME"));
        }
        else if (directoryArgument.startsWith("~")) {
            targetPath = Path.of(System.getenv("HOME") + directoryArgument.substring(1));
        }
        else {
            targetPath = Path.of(directoryArgument);
        }
        Path absolutePath = Path.of(currentDirectory).resolve(targetPath);
        //ignore the tempPath if it is absolute path
        boolean directoryExists = false;

        try{
            absolutePath = absolutePath.toRealPath();
            directoryExists = true;
        } catch (IOException e) {
            System.err.println("cd: " + directoryArgument + ": No such file or directory");
        }
        if (directoryExists) {
            currentDirectory = absolutePath.toString();
        }
    }
}
