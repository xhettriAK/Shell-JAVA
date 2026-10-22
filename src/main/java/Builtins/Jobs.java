package Builtins;

import com.google.common.collect.*;
import com.sun.security.jgss.GSSUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public  class Jobs implements BuiltInCommands{
    private static ListMultimap<String, String> jobsInfo= ArrayListMultimap.create();

    private static boolean isProcessRunning(long pid)
    {
        Optional<ProcessHandle>currentProcess = ProcessHandle.of(pid);
        return currentProcess.isPresent() &&  currentProcess.get().isAlive();

    }
    static void  printbackgroundInfo(String argumentText)
    {
        String marker = "";
        String status = "Running";
        ArrayList<Integer>jobsToDelete = new ArrayList<>();
        for(String jobs_id : Jobs.jobsInfo.keySet()) {
            List<String> info = Jobs.jobsInfo.get(jobs_id);
            if (Integer.parseInt(jobs_id) ==jobsInfo.keySet().size()) {
                marker = "+";
            }
            else if(Integer.parseInt(jobs_id) ==jobsInfo.keySet().size()-1)
            {
                marker = "-";
            }
            else{
                marker = " ";
            }
            System.out.print("[" + jobs_id + "]" + marker + "  ");

            for (int i = info.toArray().length - 1; i >=0; i--) {
                if(i==info.toArray().length-1){
                    if(!isProcessRunning(Long.parseLong(info.get(i)))) {
                        status = "Done";
                        jobsToDelete.add(Integer.parseInt(jobs_id));
                    }
                }
                else if (i == info.toArray().length - 2) {
                    System.out.format("%-24s", status);
                }
                else {
                    System.out.print(info.get(i) + "&");
                }

            }
            status="Running";
            System.out.println();
        }
        if(!jobsToDelete.isEmpty())
        {
            for(var i: jobsToDelete)
            {
                jobsInfo.removeAll(String.valueOf(i));
            }

        }
    }
    public static void putjobsInfo(int jobs_id,Long pid, String commandName, boolean status )
    {
        String stat = "Not Running";
        if(status) {
            stat = "Running";
        }

        Jobs.jobsInfo.put(String.valueOf(jobs_id),commandName);
        Jobs.jobsInfo.put(String.valueOf(jobs_id),stat);
        Jobs.jobsInfo.put(String.valueOf(jobs_id),String.valueOf(pid));

    }
}

