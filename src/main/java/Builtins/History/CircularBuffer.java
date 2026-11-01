package Builtins.History;
//Simple CircularBUffer
public class CircularBuffer {
    private static int capacity = 512;
    private static int front = 0;
    private static int rear = -1;
    private static int currentSize = 0;
    private static History.historyEntries[] hist = new History.historyEntries[capacity];

    public static void Add(History.historyEntries value) {
        if(currentSize==capacity){
            remove(value);
        }else{
            rear=(rear+1)%capacity;
            hist[rear]=value;
            currentSize++;
        }
    }
    public static void remove(History.historyEntries value) {
        hist[front]=value;
        rear = front;
        front=(front+1)%capacity;

    }
    public static String getValue(int index) {
        if (index >= 0 && index <= currentSize - 1) {
            index = (front + index) % capacity;
            return hist[index].cmd();
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
