package Shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalMode {

    private static String savedTerminalSettings = null;
    private static boolean rawModeEnabled = false;

    private static boolean saveCurrentTerminalSettings() {
        int exitStatus = -1;
        ProcessBuilder settingsProcess = new ProcessBuilder("stty", "-g");
        settingsProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = settingsProcess.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                savedTerminalSettings = reader.readLine();
            }
           exitStatus = process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("something went wrong");
        }
        return exitStatus == 0;
    }
    private static boolean enableRawMode(){
        int exitStatus = -1;

            ProcessBuilder rawModeProcess = new ProcessBuilder("stty", "-echo", "-icanon", "-isig", "-ixon", "-iexten");
            rawModeProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
            try {
               Process process = rawModeProcess.start();
               exitStatus = process.waitFor();

            } catch (IOException | InterruptedException e) {
                System.out.println("something went wrong");
            }

        return exitStatus == 0;
    }
    public static void restoreTerminalMode()
    {

        if (rawModeEnabled && savedTerminalSettings != null) {
            ProcessBuilder restoreProcess = new ProcessBuilder("stty", savedTerminalSettings);
            restoreProcess.redirectInput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process process = restoreProcess.start();
                process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                System.out.println("Type stty sane");
            }
        }

    }
    public static void initialize()
    {
        if (saveCurrentTerminalSettings() && savedTerminalSettings != null) {
             rawModeEnabled = enableRawMode();
             if(rawModeEnabled) {
                 Runtime runtime = Runtime.getRuntime();
                 //getting the runtime envirnoment
                 runtime.addShutdownHook(new Thread(TerminalMode::restoreTerminalMode));
                 //run only when program ends;
             }
        }

    }
    public boolean isRawModeEnabled()
    {
        return rawModeEnabled;
    }
}
