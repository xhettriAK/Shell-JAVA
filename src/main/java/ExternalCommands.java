import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ExternalCommands {
    public static boolean processCreation(String command,List<String> commandArgument ) {

        if (!FindPath.checkExecutable(command).isEmpty()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(commandArgument);
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                p.waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }
    public static void runCommands(String command, List<String> commandArgument) {
        if (!processCreation(command, commandArgument)) {
            System.out.println(command + ": command not found");
        }
    }
}
