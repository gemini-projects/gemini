package it.at7.gemini.dsl;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

public class Lexer {
    private StreamTokenizer input;

    public enum TokenType {
        INTERFACE("INTERFACE"),
        ENTITY("ENTITY"),
        EMBEDABLE("EMBEDABLE"),
        IMPLEMENTS("IMPLEMENTS"),
        L_BRACE("{"),
        R_BRACE("}"),
        ASTERISK("*"),
        COMMA(","),
        WORD(""),
        EOF(""),
        EOL("");

        private String keyword;

        TokenType(String keyword) {
            this.keyword = keyword;
        }

        public static TokenType getTokenFromKeyword(int type, String keyword) {
            for (TokenType tokenType : values()) {
                if (tokenType.keyword.equals(keyword)) {
                    return tokenType;
                }
            }
            switch (type) {
                case StreamTokenizer.TT_WORD:
                    return WORD;
                case StreamTokenizer.TT_EOF:
                    return EOF;
                case StreamTokenizer.TT_EOL:
                    return EOL;
            }
            return EOF;
        }
    }

    public Lexer(Reader r) {
        input = new StreamTokenizer(r);
        input.resetSyntax();
        input.wordChars('!', '~');
        input.ordinaryChar('/');
        input.whitespaceChars('\u0000', ' ');
        input.slashSlashComments(true);
        input.slashStarComments(true);
        input.eolIsSignificant(false);
        input.commentChar('#');
    }

    public TokenType nextToken() {
        try {
            return TokenType.getTokenFromKeyword(input.nextToken(), input.sval);
        } catch (IOException e) {
            e.printStackTrace();
            return TokenType.EOF;
        }
    }

    public String getVal() throws SyntaxError {
        if (input.sval == null) throw new SyntaxError("Syntax Error");
        return input.sval;
    }

}
