import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mode {

    private static String userSetting=null;
    private static boolean rawModeEnabled = false;

    private static boolean savedCurrentSetting() {
        int status  = -1;
        ProcessBuilder saved = new ProcessBuilder("stty", "-g");
        saved.redirectInput(ProcessBuilder.Redirect.INHERIT);

        try {
            Process p = saved.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                line = reader.readLine();
                userSetting = line;
            }
           status=p.waitFor();

        } catch (IOException | InterruptedException e) {
            System.out.println("something went wrong");
        }
        return status==0;
    }
    private static boolean  enableRaw(){
        int status  = -1;

            ProcessBuilder raw = new ProcessBuilder("stty", "-echo", "-icanon","-isig","-ixon","-iexten");
            raw.redirectInput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process P = raw.start();
               status =  P.waitFor();

            } catch (IOException | InterruptedException e) {
                System.out.println("something went wrong");
            }

        return status==0;
    }
    public static void disableRaw()
    {

        if(rawModeEnabled && userSetting!=null) {
            ProcessBuilder disable = new ProcessBuilder("stty", userSetting);
            disable.redirectInput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process P = disable.start();
                P.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Type stty sane");
            }
        }

    }
    public static void run()
    {
        if(savedCurrentSetting() && userSetting!=null)
        {
             rawModeEnabled = enableRaw();
             if(rawModeEnabled)
             {
                 Runtime runtime = Runtime.getRuntime();
                 //getting the runtime envirnoment
                 runtime.addShutdownHook(new Thread(Mode::disableRaw));
                 //run only when program ends;
             }
        }

    }
    public boolean getRunStatus()
    {
        return rawModeEnabled;
    }
}
