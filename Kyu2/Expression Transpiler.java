import java.util.List;
import java.util.LinkedList;
import java.util.stream.Collectors;

enum TokenType {
    ContiguousString,
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    Arrow,
    Comma
}

class Token {

    public TokenType type;
    public String string;
    
    public Token(TokenType type, String string) {
        this.type = type;
        this.string = string;
    }
    
    @Override
    public String toString() {
        if(type == TokenType.ContiguousString) return "ContiguousString";
        if(type == TokenType.LeftParen) return "LeftParen";
        if(type == TokenType.RightParen) return "RightParen";
        if(type == TokenType.LeftBrace) return "LeftBrace";
        if(type == TokenType.RightBrace) return "RightBrace";
        if(type == TokenType.Arrow) return "Arrow";
        if(type == TokenType.Comma) return "Comma";
        return "";
    }
    
    @Override
    public boolean equals(Object other) {
        if(this == other) {
            return true;
        }
        if(other instanceof Token && ((Token)other).type == type && ((Token)other).string == string) {
            return true;
        }
        return false;
    }
    
}

public class Transpiler {

    public static LinkedList<Token> tokenize(String code) {
        LinkedList<Token> tokens = new LinkedList<>();
        String delimiters = "(){}, -\n";
        StringBuilder sb = null;
        for(int i = 0; i < code.length(); ++i) {
            char c = code.charAt(i);
            if(sb != null && delimiters.indexOf(c) != -1) {
                tokens.add(new Token(TokenType.ContiguousString, sb.toString()));
                sb = null;
            }
            if(c == '(') {
                tokens.add(new Token(TokenType.LeftParen, "("));
            }
            else if(c == ')') {
                tokens.add(new Token(TokenType.RightParen, ")"));
            }
            else if(c == '{') {
                tokens.add(new Token(TokenType.LeftBrace, "{"));
            }
            else if(c == '}') {
                tokens.add(new Token(TokenType.RightBrace, "}"));
            }
            else if(c == ',') {
                tokens.add(new Token(TokenType.Comma, ","));
            }
            else if(c == '-' && i < code.length() - 1 && code.charAt(i + 1) == '>') {
                tokens.add(new Token(TokenType.Arrow, "->"));
                ++i;
            }
            else if(c != ' ' && c != '\n') {
                if(sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(c);
            }
        }
        if(sb != null) {
            tokens.add(new Token(TokenType.ContiguousString, sb.toString()));
        }
        return tokens;
    }
    
    private static Token expectToken(LinkedList<Token> tokens, TokenType expected) throws Exception {
        if(tokens.isEmpty() || tokens.peek().type != expected) {
            throw new IllegalArgumentException("Expected different token type");
        }
        return tokens.poll();
    }
    
    public static void nameOrNumber(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        Token next = expectToken(tokens, TokenType.ContiguousString);
        if(!next.string.matches("[A-Za-z_]\\w*|[1-9][0-9]*|0")) {
            throw new IllegalArgumentException("Not a name or number");
        }
        sb.append(next.string);
    }
    
    public static void lambdaparam(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        nameOrNumber(tokens, sb);
        if(tokens.peek().type == TokenType.Comma) {
            tokens.poll();
            sb.append(",");
            lambdaparam(tokens, sb);
        }
    }
    
    public static void lambdastmt(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        nameOrNumber(tokens, sb);
        sb.append(";");
        if(tokens.peek().type == TokenType.ContiguousString) {
            lambdastmt(tokens, sb);
        }
    }
    
    public static void lambda(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        expectToken(tokens, TokenType.LeftBrace);
        sb.append("(");
        if(tokens.peek().type != TokenType.RightBrace) {
            int nextArrow = tokens.indexOf(new Token(TokenType.Arrow, "->"));
            int nextRBrace = tokens.indexOf(new Token(TokenType.RightBrace, "}"));
            if(nextArrow != -1 && nextArrow < nextRBrace) {
                lambdaparam(tokens, sb);
                expectToken(tokens, TokenType.Arrow);
            }
        }
        sb.append("){");
        if(tokens.peek().type != TokenType.RightBrace) {
            lambdastmt(tokens, sb);
        }
        sb.append("}");
        expectToken(tokens, TokenType.RightBrace);
    }
    
    public static void expression(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        if(tokens.peek().type == TokenType.ContiguousString) {
            nameOrNumber(tokens, sb);
        }
        else {
            lambda(tokens, sb);
        }
    }
    
    public static void parameters(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        expression(tokens, sb);
        if(tokens.peek().type == TokenType.Comma) {
            tokens.poll();
            sb.append(",");
            parameters(tokens, sb);
        }
    }
    
    public static void function(LinkedList<Token> tokens, StringBuilder sb) throws Exception {
        expression(tokens, sb);
        sb.append("(");
        if(tokens.peek().type == TokenType.LeftParen) {
            tokens.poll();
            boolean foundParams = false;
            if(tokens.peek().type != TokenType.RightParen) {
                parameters(tokens, sb);
                foundParams = true;
            }
            expectToken(tokens, TokenType.RightParen);
            if(!tokens.isEmpty()) {
                if(foundParams) {
                    sb.append(",");
                }
                lambda(tokens, sb);
            }
        }
        else {
            lambda(tokens, sb);
        }
        sb.append(")");
        if(!tokens.isEmpty()) {
            throw new IllegalArgumentException("Unexpected additional characters");
        }
    }

    public static String transpile(String code) {
        LinkedList<Token> tokens = tokenize(code);
        StringBuilder sb = new StringBuilder();
        try {
            function(tokens, sb);
        }
        catch(Exception ex) {
            return "";
        }
        return sb.toString();
    }
    
}
