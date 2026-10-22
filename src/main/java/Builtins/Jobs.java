package Builtins;
import com.google.common.collect.*;
import java.util.*;

public  class Jobs implements BuiltInCommands{

    private  static HashMap<Integer, jobsInfo>listJobs = new LinkedHashMap<>();
    private record jobsInfo( Long pid, String commandName,  int status)
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
    public static void  printBackgroundInfo()
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
            jobIdToRemove.clear();
        }


    }
    public static boolean isJobListEmpty ()
    {
       return listJobs.isEmpty();
    }
    public static void Reap()
    {
        int index = 0;
        String marker = "";
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
            index++;
            if(!listJobs.get(ID).Status())
            {
                jobIdToRemove.add(ID);
                System.out.print("["+ID+"]"+marker+"  ");
                System.out.format("%-24s","Done");
                System.out.print(listJobs.get(ID).commandName);
                System.out.println();
            }

        }
        if(!jobIdToRemove.isEmpty())
        {
            for(var Id: jobIdToRemove)
            {
                listJobs.remove(Id);
            }
            jobIdToRemove.clear();
        }
    }
    public static  int getLastJobId()
    {   int index = 0;

        for(var i : listJobs.keySet())
        {
            if (i == listJobs.size()) {
                index= i;
            }
        }
        return index;
    }
    public static void putJobsInfo(int jobs_id, Long pid, String commandName, boolean status )
    {

         listJobs.put(jobs_id,new jobsInfo(pid,commandName,1));

    }
}

