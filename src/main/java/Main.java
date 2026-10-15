import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws Exception {
        Mode.run();
        try(BufferedReader reader= new BufferedReader(new InputStreamReader(System.in)))
        {
           Shell.run(reader);

        } catch (Exception e) {
            System.out.println("something went wrong");
        }

    }
}
