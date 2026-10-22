package Builtins;

import com.google.common.collect.*;

import java.util.*;

public  class Jobs implements BuiltInCommands{
    public  static HashMap<Integer, jobsInfo>listJobs = new LinkedHashMap<>();
    public record jobsInfo( Long pid, String commandName,  int status)
    {

        public boolean Status() {
            return isProcessRunning(pid);
        }
    }
    public static ArrayList<Integer>jobIdToRemove = new ArrayList<>();
    private static boolean isProcessRunning(long pid)
    {
        Optional<ProcessHandle>currentProcess = ProcessHandle.of(pid);
        return currentProcess.isPresent() &&  currentProcess.get().isAlive();

    }
    static void  printBackgroundInfo(String argumentText)
    {
        String marker = "";
        String status = "";
        int index = 0;
        for(var ID : listJobs.keySet())
        {
            if( index == listJobs.size()-1)
            {
                marker = "+";
            }
            else if( index == listJobs.size()-2) {
                marker = "-";
            }
            else{
                marker = " ";
            }

            if(!listJobs.get(ID).Status())
            {
                status = "Done";
                jobIdToRemove.add(ID);
            }
            else{
                status= "Running";
            }
            index++;
            System.out.print("["+ID+"]"+marker+"  ");
            System.out.format("%-24s",status);
            System.out.print(listJobs.get(ID).commandName);
            if(status.equals("Running"))
            {
                System.out.print("&");
            }
            System.out.println();
        }
        if(!jobIdToRemove.isEmpty())
        {
            for(var Id: jobIdToRemove)
            {
                listJobs.remove(Id);
            }
        }

    }
    public static void putJobsInfo(int jobs_id, Long pid, String commandName, boolean status )
    {
        String stat = "Not Running";
        if(status) {
            stat = "Running";
        }
         listJobs.put(jobs_id,new jobsInfo(pid,commandName,1));
    }
}

