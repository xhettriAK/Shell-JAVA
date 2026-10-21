package Builtins;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

public class Complete  implements BuiltInCommands{
    private static HashMap<String, String> completorList = new HashMap<>();

     static void printCompletion(String argumentText) {
        String[] CompletionFunction = {"-p", "-C","-r"};
        String[] CompletionArguments = argumentText.trim().split(" ");
        String Value ="";

        StringBuilder commandBuilder = new StringBuilder();
        boolean doRest = true;
        int pMask = 1;
        int cMask = 2;
        int rMask = 3;
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
            else if(CompletionArguments[i].equals(CompletionFunction[2])){
                flag.set(rMask,true);
            }
            else
            {
                commandBuilder.append(CompletionArguments[i]);
            }
        }
        String command = commandBuilder.toString();

        if(doRest) {

            if (flag.cardinality()>1) {
                System.out.println("complete cant have multiple commands");
            } else if (flag.get(pMask)) {
                if (completorList.isEmpty() && command.isEmpty()) {
                    System.out.print("");
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
            else if(flag.get(rMask)) {
                if (completorList.isEmpty() && command.isEmpty()) {
                    System.out.print("");
                } else {
                    if (!command.isEmpty() && completorList.get(command) != null) {
                        completorList.remove(command);

                    } else if (!command.isEmpty() && completorList.get(command) == null) {
                        System.out.println("complete: " + command + ": no completion specification");
                    } else {
                        for (String key : completorList.keySet()) {
                            completorList.remove(key);
                        }
                    }

                }
            }
        } else{
            System.out.println();
        }
        flag.set(cMask,false);
        flag.set(pMask,false);
        flag.set(rMask,false);
    }
    public static  boolean commandExistInCompleter(String userInput) {
        return completorList.get(userInput.strip())!=null;
    }
    public static ArrayList<String> executeCompletorCommand(StringBuilder userInput)
    {
        String argv1,argv2="", argv3="";
        argv1 = userInput.substring(0,userInput.indexOf(" "));
        String[] argvs = userInput.toString().split(" ");
        if(argvs.length>1) {
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
        ArrayList<String>arg = new ArrayList<>();
        arg.add(argv1);
        arg.add(argv2);
        arg.add(argv3);
        return arg;

    }
    public static String getvalueFromList(String argv1) {

         return completorList.get(argv1);
    }
}
