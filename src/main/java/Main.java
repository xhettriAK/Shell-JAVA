import Shell.CommandShell;
import Shell.TerminalMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        TerminalMode.initialize();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
           CommandShell.run(reader);

        } catch (Exception e) {
            e.getCause();
        }

    }
}
