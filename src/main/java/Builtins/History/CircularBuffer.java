package Builtins.History;
//Simple CircularBUffer
public class CircularBuffer {
    private static int capacity = 512;
    private static int front = 0;
    private static int rear = -1;
    private static int currentSize = 0;
    private static String[] hist = new String[capacity];
    public static void Add(String value) {
        if(currentSize==capacity){
            remove(value);

        }else{
            rear=(rear+1)%capacity;
            hist[rear]=value;
            currentSize++;
        }

    }
    public static void remove(String value) {
        hist[front]=value;
        rear = front;
        front=(front+1)%capacity;

    }
    public static String getValue(int index) {
        if (index >= 0 && index <= currentSize - 1) {
            index = (front + index) % capacity;
            return hist[index];
        }
        return null;
    }
    public static int getFront() {
        return front;
    }
    public static int getCurrentSize(){
        return currentSize;
    }

}
