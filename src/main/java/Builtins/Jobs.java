package Builtins;

import com.google.common.collect.*;
import com.sun.security.jgss.GSSUtil;

import java.util.ArrayList;
import java.util.List;

public  class Jobs implements BuiltInCommands{
    private static ListMultimap<String, String> jobsInfo= ArrayListMultimap.create();

    static void  printbackgroundInfo(String argumentText)
    {
        String marker = "-";

        for(String jobs_id : Jobs.jobsInfo.keySet()) {
            List<String> info = Jobs.jobsInfo.get(jobs_id);
            if (Iterables.getLast(jobsInfo.keySet()).equals(jobs_id)) {
                marker = "+";
            }
            System.out.print("[" + jobs_id + "]" + marker + "  ");

            for (int i = info.toArray().length - 1; i > 0; i--) {
                if (i == info.toArray().length - 1) {
                    System.out.format("%-24s", info.get(i));
                } else {
                    System.out.print(info.get(i) + "&");
                }

            }
            System.out.println();
        }
    }
    public static void putjobsInfo(int jobs_id,Long pid, String commandName, boolean status )
    {
        String stat = "Not Running";
        if(status) {
            stat = "Running";
        }

        Jobs.jobsInfo.put(String.valueOf(jobs_id),String.valueOf(pid));
        Jobs.jobsInfo.put(String.valueOf(jobs_id),commandName);
        Jobs.jobsInfo.put(String.valueOf(jobs_id),stat);

    }
}

