import Shell.CommandShell;
import Shell.TerminalMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        TerminalMode.initialize();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8)))
        {
           CommandShell.run(reader);

        } catch (Exception e) {
            e.getCause();
        }

    }
}
