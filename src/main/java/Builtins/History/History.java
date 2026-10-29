package Builtins.History;

import Builtins.BuiltInCommands;
import com.sun.jdi.Value;

public class History implements BuiltInCommands {
    public static void run(String command){
        if(command.isBlank()){
            print();
        }
    }
    public static void create(String Value) {
        CircularBuffer.Add(Value);
    }
    public static void print() {
        int size = CircularBuffer.getCurrentSize();
        for( int i = 0 ; i<size; i++){
            String value = CircularBuffer.getValue(i);
            if(value==null){
                System.out.println("something went wrong");
                break;
            }
            System.out.println(i+1+" "+ value);

        }

    }
}
