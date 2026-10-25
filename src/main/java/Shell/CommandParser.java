package Shell;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {


    private  StringBuilder userBuilder = new StringBuilder();
    private boolean ordinary = false;
    char currentQuote = '\0';
    private String outputFile = "", errorFile = "";
    private List<String> usrList = new ArrayList<>();
    private boolean thereRedirect = false;
    private boolean thereRedirectReverse = false;
    private boolean appendError = false;
    private boolean appendOutput = false;
    int pipeStatus = 0;   /*  pipeStatus
                              0 means there is no pipe
                              1 means there is correct pipe
                              -1 means there is incorrect pipe
                             */
    private int i = 0;
    private char c=' ';


    private boolean  handleQuote(char c, char next)
    {
        if (c == '\\' && !ordinary && currentQuote == '"' && (next == '"' || next == '\\')) {
            userBuilder.append(next);
            i++;
            return true;
        }
        // when  c is \\ for the first time
         if (c == '\\' && !ordinary && currentQuote == '\0') {
            ordinary = true;
             return true;
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
             return true;
        }
        return false;
    }
    private boolean Append(char c, char next,  String userInput){
        if ((c == '1' || c == '2') && next == '>' && userBuilder.isEmpty()) {
            if (c == '1') {
                thereRedirect = true;
                if (i + 2 < userInput.length() && userInput.charAt(i + 2) == '>') {
                    appendOutput = true;
                    i++;
                } else {
                    appendOutput = false;
                }
            } else {
                thereRedirectReverse = true;
                if (i + 2 < userInput.length() && userInput.charAt(i + 2) == '>') {
                    appendError = true;
                    i++;
                } else {
                    appendError = false;
                }
            }
            i++;
            return true;
        }
        else if (c == '>') {
            if (next == '>') {
                appendOutput = true;
                i++;
            } else{
                appendOutput=false;
            }
            if(!userBuilder.isEmpty()) {
                file();
            }
            thereRedirect = true;
            return true;
        }
        return false;
    }
    public void file(){
        if (thereRedirect) {
            outputFile = userBuilder.toString();
            thereRedirect = false;
        } else if (thereRedirectReverse) {
            errorFile = userBuilder.toString();
            thereRedirectReverse = false;
        } else {
            usrList.add(userBuilder.toString());
        }
    }
    private void resetField(boolean isthisfirstime)
    {
        if (!isthisfirstime) {
             userBuilder = new StringBuilder();
            ordinary = false;
            currentQuote = '\0';
            outputFile = "";
            errorFile = "";
            usrList.clear();
            thereRedirect = false;
            thereRedirectReverse = false;
            appendError = false;
            appendOutput = false;
            pipeStatus = 0;

            int i = 0;
        }

    }
    public  List<String>  parseCommandLine(String userInput,boolean isthisfirsttime) {
        resetField(isthisfirsttime);
        for (i = 0; i < userInput.length(); i++) {
            c = userInput.charAt(i);
            char next = i + 1 < userInput.length() ? userInput.charAt(i + 1) : '\0';
            /* Inside double quotes, only escape a quote or another backslash.
                it will put string to list depending whether isthereredirect
                is there reverse
            */
            if(handleQuote(c, next)){
                continue;
            };
            if( c =='|' && currentQuote== '\0') {
                int lastPipeIndex = i;
                if(i==0 && userBuilder.isEmpty())
                {
                    pipeStatus =-1;
                }
                else if( pipeStatus==0 && !usrList.getFirst().isEmpty()){
                    pipeStatus=1;
                }
                else{
                    if( next =='|')
                    {
                        pipeStatus = -1;
                    }
                    else{
                        try {
                            if(!userBuilder.isEmpty()&&userInput.charAt(i-1)!='|') {
                                pipeStatus=1;
                            }
                            else if (!usrList.get(lastPipeIndex + 1).isEmpty() || !usrList.get(lastPipeIndex + 2).isEmpty()) {
                             pipeStatus = 1;
                            }
                        } catch (IndexOutOfBoundsException e) {
                         pipeStatus = -1;
                         System.out.print("");
                        }
                    }
                }
            }
            if (c == ' ' || c == '\t') {
                if (ordinary || currentQuote != '\0') {
                    userBuilder.append(c);
                    ordinary = false;
                } else if (!userBuilder.isEmpty()) {
                    file();
                    userBuilder.setLength(0);
                }
                continue;
            }
            // check only when there is no backslash
            if (currentQuote == '\0' && !ordinary) {
                if(Append(c, next, userInput)) {
                    continue;
                }
            }
            userBuilder.append(c);
            ordinary = false;
        }
        if (!userBuilder.isEmpty()) {
            file();
        }
        return usrList;
    }

    public boolean isAppendError() {
        return appendError;
    }
    public boolean isAppendOutput() {return appendOutput;}
    public String getOutputFile()   {return outputFile;}
    public String getErrorFile()    {return errorFile;}
    public char getCurrentQuote()   {return currentQuote;}
    public int getPipeStatus()      {return pipeStatus;}
    public char getLastChar()        {return c;}

}
