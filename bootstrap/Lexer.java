import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class Lexer {
    private String _filename;
    private int _line = 1;
    private FileInputStream _src;
    private Token _ahead = null;
    private final int NOTHING = -2;
    private int _cahead = NOTHING;

    public Lexer(String filename)
    throws FileNotFoundException {
        _filename = filename;
        _src = new FileInputStream(filename);
    }

    public int line() {
        return _line;
    }

    public Token nextToken() {
        if (_ahead != null) {
            var token = _ahead;
            _ahead = null;
            return token;
        } else {
            return readToken();
        }
    }

    public Token peekToken() {
        _ahead = nextToken();
        return _ahead;
    }

    private int nextc() {
        try {
            if (_cahead != NOTHING) {
                var c = _cahead;
                _cahead = NOTHING;
                return c;
            } else {
                return _src.read();
            }
        } catch (IOException e) {
            _cahead = NOTHING;
            return -1;
        }
    }

    private int getc() {
        var c = nextc();
        if (c == '\n') {
            _line++;
        }
        return c;
    }

    private int peekc() {
        _cahead = nextc();
        return _cahead;
    }

    private int firstRelevant() {
        while (true) {
            var c = getc();
            if (!isIrrelevantChar(c)) {
                return c;
            }
            if (c == ';') { // comment character
                do {
                    c = getc();
                } while (c != '\n' && c != -1);
            }
        }
    }

    private boolean isIrrelevantChar(int c) {
        switch (c) {
        case ' ': case '\n': case ';': case '\t': {
            return true;
        }
        }
        return false;
    }

    private boolean isSpecialTokenChar(int c) {
        switch (c) {
        case '@': case '^': case '(': case ')': case '[': case ']':
        case '{': case '}': case '$': case '\'': case ':': {
            return true;
        }
        }
        return false;
    }
    
    private boolean isIdentifierChar(int c) {
        if (isSpecialTokenChar(c) || isIrrelevantChar(c)) {
            return false;
        }
        switch (c) {
        case '#': case '%': case '&': case '|': case '"':
        case ',': case '\\': case '.': {
            return false;
        }
        }
        return true;
    }

    private Token stringToken() {
        var str = "";
        while (true) {
            var c = getc();
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                c = getc();
                switch (c) {
                case 'n': {
                    c = '\n';
                    break;
                }
                case 't': {
                    c = '\t';
                    break;
                }
                }
            }
            if (c == -1) {
                Error.add("end of file in string literal",
                          _filename + ": " + _line);
                break;
            }
            str += c;
        }
        return new Token(Token.Kind.STR, str, _filename, _line);
    }

    private Token charToken() {
        var c = getc();
        if (c == '\\') {
            c = getc();
            switch (c) {
            case 'n' : {
                c = '\n';
                break;
            }
            case 't' : {
                c = '\t';
                break;
            }
            case '\\' : {
                c = '\\';
                break;
            }
            default: {
                Error.add("unknown character literal: \\" + ((char) c),
                          _filename + ":" + _line);
            }
            }
        }
        return new Token(Token.Kind.INT, Integer.toString(c),
                         _filename, _line);
    }

    private Token readToken() {
        var c = firstRelevant();
        if (isSpecialTokenChar(c)) {
            return new Token(Token.Kind.SPECIAL, String.valueOf((char) c),
                             _filename, _line);
        }
        switch (c) {
        case '"': { // string
            return stringToken();
        }
        case '~' : { // character
            return charToken();
        }
        case -1: { // eof
            return new Token(Token.Kind.EOF, "", _filename, _line);
        }
        default: {
            // TODO
            return null;
        }
        }
        // not reached
    }
}
