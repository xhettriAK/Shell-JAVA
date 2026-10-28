package Token;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public record CommandInformation(List<String> arguments, TokenType outputType, String outputFile,
                                     TokenType errorType, String errorFile) { }
    private record RedirectInfo(TokenType type, String fileName) { }

    private final List<Token> tokens;
    private int current = 0;
    private boolean incomplete = false;
    private boolean runInBackground = false;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    //RECURSIVE DESCENT PARSING

    public List<CommandInformation> parse()  {
        return parseLine();
    }

    private List<CommandInformation> parseLine()  {
        List<CommandInformation> pipeline = parsePipeline();
        if (peek().type() == TokenType.BACKGROUND) {
            runInBackground = true;
            advance();
        }
        expect(TokenType.END);
        return pipeline;
    }

    private List<CommandInformation> parsePipeline() {
        List<CommandInformation> pipeline = new ArrayList<>();
        pipeline.add(parseCommand());
        while (peek().type() == TokenType.PIPE) {
            advance();
            if (peek().type() == TokenType.END) {
                incomplete = true;
                break;
            }
            pipeline.add(parseCommand());
        }
        return pipeline;
    }

    private CommandInformation parseCommand() {
        List<String> arguments = new ArrayList<>();
        String outputFile = "";
        String errorFile = "";
        TokenType outputType = TokenType.NONE;
        TokenType errorType = TokenType.NONE;
        Token token = match(TokenType.WORD, TokenType.REDIRECT_ERR, TokenType.REDIRECT_OUT,
                TokenType.APPEND_OUT, TokenType.APPEND_ERR);
        while (token != null) {
            if (token.type() == TokenType.WORD) {
                arguments.add(token.literal());
            } else {
                RedirectInfo redirect = parseRedirect(token);
                if (redirect.type() == TokenType.REDIRECT_OUT || redirect.type() == TokenType.APPEND_OUT) {
                    outputFile = redirect.fileName();
                    outputType = redirect.type();
                } else {
                    errorFile = redirect.fileName();
                    errorType = redirect.type();
                }
            }
            token = match(TokenType.WORD, TokenType.REDIRECT_ERR, TokenType.REDIRECT_OUT,
                    TokenType.APPEND_OUT, TokenType.APPEND_ERR);
        }
        if (arguments.isEmpty()) {
            Token peeked = peek();
            throw new ParserError(peeked.offset(), peeked.length(), "expected a “unexpected token "+peek().literal());
        }
        return new CommandInformation(arguments, outputType, outputFile, errorType, errorFile);
    }

    private RedirectInfo parseRedirect(Token redirectToken)  {
        Token fileToken = advance();
        if (fileToken.type() == TokenType.WORD) {
            return new RedirectInfo(redirectToken.type(), fileToken.literal());
        } else {
            throw new ParserError(fileToken.offset(),fileToken.length() ,"expected a filename after " +redirectToken.literal());
        }
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        int previous = current;
        if (current < tokens.size() - 1) {
            ++current;
        }
        return tokens.get(previous);
    }

    private Token match(TokenType... types) {
        for (var type : types) {
            if (peek().type() == type) {
                return advance();
            }
        }
        return null;
    }

    private boolean check(TokenType type) {
        return peek().type() == type;
    }

    private void expect(TokenType type) {
        Token token = peek();
        if (token.type() == type) {
            advance();
        } else {
            if(type==TokenType.END) {
                throw new ParserError(token.offset(), token.length(), "unexpected END OF LINE ");
            }else {
                throw new ParserError(token.offset(), token.length(), "unexpected token " + token.literal());
            }
        }
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public boolean isRunInBackground() {
        return runInBackground;
    }
}
