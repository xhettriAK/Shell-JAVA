import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws Exception {
        TerminalMode.initialize();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)))
        {
           CommandShell.run(reader);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
