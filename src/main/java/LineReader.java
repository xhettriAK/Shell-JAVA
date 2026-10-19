import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LineReader {
    private  boolean previousKeyWasTab = false;
    private  StringBuilder inputBuilder = new StringBuilder();
    private boolean isDirectory = false;
    private boolean fileOrDirectory = false;

    private  void  handleBackSpace()
    {
        if (!inputBuilder.isEmpty()) {

            inputBuilder.deleteCharAt(inputBuilder.length() - 1);
            System.out.print("\b \b");

        }
        previousKeyWasTab = false;
    }
    private void handleEnter(){
        previousKeyWasTab = false;
        System.out.println();
    }
    private void handlePrintableCharacter(char inputCharacter)
    {
        previousKeyWasTab = false;
        System.out.print(inputCharacter);
        inputBuilder.append(inputCharacter);

    }
    private void handleTab(char inputCharacter) {
        previousKeyWasTab = false;
        if (inputBuilder.isEmpty()) {
            //tab{if(userBuilder.isEmpty())
            inputBuilder.append(inputCharacter);
        } else {
            if (inputBuilder.toString().contains(" ")) {
                fileOrDirectory = true;
                String prefix =inputBuilder.substring(inputBuilder.lastIndexOf(" ") + 1);
                String directory = "";
                String filePrefix = "";
                int lastSlash = prefix.lastIndexOf('/');
                if(prefix.contains("/")){
                    if (lastSlash == prefix.length() - 1) {
                        filePrefix = "";
                        directory = prefix;
                    } else if (lastSlash != -1) {
                        filePrefix = prefix.substring(lastSlash + 1);
                        if (!filePrefix.isEmpty()) {
                            directory = prefix.substring(0, prefix.lastIndexOf(filePrefix));
                        }

                    } else{
                        filePrefix = prefix;
                        directory = prefix;
                    }
                }
                else{
                    filePrefix = prefix;
                }

                Set<String> matches = PathUtils.findFileCompletions(directory,filePrefix);
                if(matches.size()==1)
                {
                    if(Files.isDirectory(Path.of(directory + matches.iterator().next())))
                    {
                        isDirectory = true;
                    }
                }
                applyCompletionMatches(filePrefix,matches);

            } else {
                String completion = BuiltInCommands.completeBuiltInCommand(inputBuilder.toString());
                if (completion != null) {
                    inputBuilder.append(completion).append(' ');
                    System.out.print(completion + ' ');
                } else {
                    Set<String> matches = ExternalCommandRunner.findExternalCommandCompletions(inputBuilder.toString());
                    applyCompletionMatches(inputBuilder.toString(),matches);
                }
            }
        }
    }

    public  String readUserInput(BufferedReader reader) throws IOException
    {
        char inputCharacter;
        int inputCode;

        ValueRange printableCharacters = ValueRange.of(32, 126);

        while (((inputCode = reader.read()) != -1)) {

            inputCharacter = (char) inputCode;

            if (printableCharacters.isValidValue(inputCode)) {
                handlePrintableCharacter(inputCharacter);
            } else {
                if (inputCharacter == '\u0008' || inputCharacter == 127)
                {
                    handleBackSpace();
                }
                if (inputCharacter == '\n') {
                    handleEnter();
                    break;
                }
                if(inputCharacter=='\t')
                {
                    handleTab(inputCharacter);
                }
            }
        }

        return inputBuilder.toString();
    }
    private void applyCompletionMatches(String userPrefix, Set<String>matches) {
        if (!matches.isEmpty()) {
            String commonPrefix = "";
            if (matches.size() > 1) {
                commonPrefix = findCommonPrefix(new ArrayList<>(matches));
                if (!commonPrefix.isEmpty()) {
                    if(fileOrDirectory)
                    {
                        if (inputBuilder.substring(inputBuilder.indexOf(" ")).equals(commonPrefix) && !previousKeyWasTab) {

                            System.out.print("\u0007");
                            previousKeyWasTab = true;
                        }
                        fileOrDirectory =false;
                    }
                    else {
                        if (inputBuilder.toString().equals(commonPrefix) && !previousKeyWasTab) {
                            System.out.print("\u0007");
                            previousKeyWasTab = true;
                        }
                    }
                }
            }
            if ((commonPrefix.isEmpty() || userPrefix.equals(commonPrefix))) { //commonPrefix is excatly one or there is no commonPrefix

                inputBuilder.append(completeOrListMatches(userPrefix, matches));
            } else {
                String completionText = commonPrefix.substring(inputBuilder.toString().length()); //commonPrefix is longer than user
                inputBuilder.append(completionText);
                System.out.print(completionText);
            }
        }
        else{
            if (!previousKeyWasTab) { //no matches
                System.out.print("\u0007");
            }
        }
    }
    private  String completeOrListMatches(String prefix, Set<String> matches)
    {  //prefix is filename for file | prefix is inputBuilder for external command
        String completionSuffix = "";
        if (matches.size() == 1) {
            for (var match : matches) {
                if(isDirectory) {
                    completionSuffix = match.substring(prefix.length()).stripTrailing() +'/';
                    isDirectory =false;
                }else{
                    completionSuffix = match.substring(prefix.length()) +' ';
                }
                System.out.print(completionSuffix);

            }
        } else {
            if(!previousKeyWasTab) {
                System.err.print("2");
                System.out.println();
                for (var match : matches) {
                    System.out.print(match + " ");
                }
                System.out.println();
                System.out.print("$ " + inputBuilder);
                return "";
            }
        }
        return completionSuffix;

    }

    private  String findCommonPrefix(List<String> sortedMatches)
    //list is sorted| Comparing first and last
    {
        StringBuilder prefix = new StringBuilder();
        String firstWord = sortedMatches.getFirst();
        String lastWord = sortedMatches.getLast();
        for (int i = 0; i < firstWord.length(); i++) {
            if (firstWord.charAt(i) != lastWord.charAt(i)) {
                return prefix.toString();
            } else {
                prefix.append(lastWord.charAt(i));
            }
        }
        return prefix.toString();
    }
}

