package Token;

public class ParserError extends RuntimeException {
    int offset;
    int length;
    String message;

    public ParserError(int offset, int length, String s) {
        this.offset= offset;
        this.length = length;
        this.message = s;
    }

    public int getLength() {
        return length;
    }
    public int getOffset(){
        return offset;
    }
    public String getMessage(){
        return message;
    }
}
