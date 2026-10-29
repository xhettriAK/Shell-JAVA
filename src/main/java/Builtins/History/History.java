package Builtins.History;

import Builtins.BuiltInCommands;
import com.sun.jdi.Value;

public class History implements BuiltInCommands {
    public static void run(String command){
        if(!command.isBlank()){
            int entries = 0;
            try {
                entries = Integer.parseInt(command);
                print(entries);
            } catch (NumberFormatException e) {
                System.out.print(" ");
            }

        }else{
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
    public static void print(int entries) {
        int size = CircularBuffer.getCurrentSize();
        int front = CircularBuffer.getFront();
        for( int i = entries ; i>=1; i++){
            int recent = size-i;
            int index = (front+recent)%size;
            String value = CircularBuffer.getValue(index);
            if(value==null){
                System.out.println("something went wrong");
                break;
            }
            System.out.println(i+1+" "+ value);

        }

    }
}
