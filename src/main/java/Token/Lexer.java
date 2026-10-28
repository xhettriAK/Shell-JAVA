package Token;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    final String source;
    final List<Token> tokens = new ArrayList<>();
    private  int current = 0;
    private int start = 0;
    private boolean inComplete = false;

    public Lexer(String source) {

        this.source = source;
    }

   public List<Token> scanTokens() {
       while (!isAtEnd()) {
           start = current;
           scanToken();
       }
       tokens.add(new Token(TokenType.END, "", "", current, 0));
       return tokens;
   }
   private boolean isAtEnd() {
       return current >= this.source.length();
   }

    private void scanToken() {
        char currentChar = advance();
        switch (currentChar) {
            case '|' -> addToken(TokenType.PIPE);
            case '>' -> redirect(currentChar);
            case '1', '2' -> numberedRedirect(currentChar);
            case '&' -> addToken(TokenType.BACKGROUND);
            case '\t', ' ' -> {}
            default -> word(currentChar);
        }
    }

    private void numberedRedirect(char currentChar) {
        char next = peek();
        if ((next == '>')) {
            current++;
            char peekNext = peek();
            if (peekNext == '>') {
                current++;
                if (currentChar == '1') {
                    addToken(TokenType.APPEND_OUT, source.substring(start, current));
                } else {
                    addToken(TokenType.APPEND_ERR, source.substring(start, current));
                }
            } else {
                if (currentChar == '1') {
                    addToken(TokenType.REDIRECT_OUT, source.substring(start, current));
                } else {
                    addToken(TokenType.REDIRECT_ERR, source.substring(start, current));
                }

            }
        } else {
            word(currentChar);
        }
    }

    private void redirect(char currentChar) {
        char next = peek();
        if (next == '>') {
            current++;
            addToken(TokenType.APPEND_OUT, source.substring(start, current));

        } else {
            addToken(TokenType.REDIRECT_OUT, source.substring(start, current));
        }

    }

    private void word(char currentChar) {
        StringBuilder wordBuilder = new StringBuilder();

        char currentQuote = '\0';
        char next;
        char c = currentChar;
        boolean currentCharinWord = true;
        while (currentCharinWord) {
            next = peek();
            switch (c) {
                case '\'', '\"' -> {
                    if (currentQuote == '\0') {
                        currentQuote = c;
                    } else if (currentQuote == c) {
                        currentQuote = '\0';
                    } else {
                        wordBuilder.append(c);
                    }
                }
                case '\\' -> {
                    if (currentQuote == '\0') {
                        if (next == '\0') {
                            inComplete = true;
                            currentCharinWord = false;
                        } else {
                            wordBuilder.append(next);
                            current++;
                        }
                    } else if (currentQuote == '"' && (next == '"' || next == '\\')) {
                        wordBuilder.append(next);
                        current++;
                    } else {
                        wordBuilder.append(c);
                    }
                }
                default -> wordBuilder.append(c);
            }
            next = peek();
            //peeking again one more time , so that we can check one more time;

            if (currentQuote != '\0' && next == '\0') {
                inComplete = true;
                currentCharinWord = false;
            } else if (currentQuote == '\0' && (next == '\0' ||
                    next == '|' || next == ' ' || next == '&' || next == '>' || next == '\t')) {
                currentCharinWord = false;
            } else {
                c = advance();
            }
        }
        addToken(TokenType.WORD, wordBuilder.toString());
    }

    private char peek() {

        return (current < source.length()) ? source.charAt(current) : '\0';
    }

    private char advance() {

        return this.source.charAt(current++);
    }

    private void addToken(TokenType type) {

        addToken(type, "");
    }

    private void addToken(TokenType type, String literal) {

        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, start, current - start));
    }

    public boolean isInComplete() {
        return inComplete;
    }
}
