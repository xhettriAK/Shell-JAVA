package Token;

/*
    We are making this record because we need to know
    about TokenType, String lexeme , String Literal , offset and length
    for example:
    if there is | , WE  will get token of PIPE
    LEXME IS | AND LITERAL IS PIPE
    I also added offset and length
    so that we can show where the error happened just like in fish shell

 */

public record Token ( TokenType type, String lexeme,String literal ,int offset, int length){

    public String toString() {
        return type + " " + lexeme + " " + literal+"  "+ offset+" "+length;
    }
}
