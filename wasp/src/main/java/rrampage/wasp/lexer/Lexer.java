package rrampage.wasp.lexer;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

public class Lexer {
    private final String input;
    private final char[] cs;
    private static final Set<Character> WS_CHARS = Set.of(' ', '\t', '\r', '\n');
    private static final Set<Character> ID_CHARS = Set.of("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&\\*+-./:<=>?@'^_`|~".chars().mapToObj(c -> (char)c).toArray(Character[]::new));

    private char[] get(int start, int n) {
        if (n < 0 || start < 0 || start >= cs.length || start + n >= cs.length) {
            System.out.println("NULL");
            return null;
        }
        return Arrays.copyOfRange(cs, start, start+n);
    }
    public Lexer(String input) {
        this.input = input;
        this.cs = input.toCharArray();
    }
    public Result<Optional<Token>, LexError> parse(int pos) {
        AtomicInteger apos = new AtomicInteger(pos);
        var result = parseKind(apos);
        return (result.isError()) ?
                new Result.Error<>(result.err().get()) :
                new Result.Ok<>(result.ok().map(tk -> new Token(tk.get(), pos, apos.get()- pos)));
    }

    private int skipWhitespace(AtomicInteger pos) {
        int i = pos.get(), start = pos.get();
        while (i < cs.length) {
            i++;
            if (i >= cs.length || !WS_CHARS.contains(cs[i])) {
                break;
            }
        }
        pos.set(i);
        return i - start;
    }

    private String splitUntil(AtomicInteger pos, char c) {
        int i = pos.get(), start = pos.get();
        while (i < cs.length) {
            i++;
            if (cs[i] == c) {
                break;
            }
        }
        pos.set(i);
        return input.substring(start, i);
    }

    private  Result<SimpleEntry<TokenKind.ReservedKind, String>, LexError> parseReserved(AtomicInteger pos) {
        var idchars = false;
        var strings = 0;
        int i = pos.get(), start = pos.get();
        while (i < cs.length) {
            i++;
            char c = cs[i];
            if (ID_CHARS.contains(c)) {
                idchars = true;
                // *pos += 1
            } else if (c == '"') {
                strings++;
                // *pos += 1
                // TODO : parseString and check result
                AtomicInteger strPos = new AtomicInteger(i);
                var res = parseString(strPos);
                i = strPos.get();
                if (res.isError()) {
                    return new Result.Error<>(res.err().get());
                }
                System.out.println("PARSED_STRING: " + res.ok().orElseGet(() -> "EMPTY_STR"));
            } else {
                break;
            }
        }
        var ret = input.substring(start, i);
        pos.set(i);
        if (!idchars) {
            if (strings == 0) {
                return new Result.Error<>(new LexError.UnexpectedEof());
            }
            return new Result.Ok<>(new SimpleEntry<>(TokenKind.ReservedKind.String, ret));
        } else {
            if (strings == 0) {
                return new Result.Ok<>(new SimpleEntry<>(TokenKind.ReservedKind.IdChars, ret));
            }
            return new Result.Ok<>(new SimpleEntry<>(TokenKind.ReservedKind.Reserved, ret));
        }
    }

    public Result<String, LexError> parseString(AtomicInteger pos) {
        // TODO
        return null;
    }

    public Optional<TokenKind> classifyNumber(String s) {
        SignToken st = null;
        int numPos = 0;
        if (s.startsWith("+")) {
            st = SignToken.Plus;
            numPos = 1;
        }
        if (s.startsWith("-")) {
            st = SignToken.Minus;
            numPos = 1;
        }
        boolean negative = SignToken.Minus.equals(st);
        String num = (numPos == 0) ? s : s.substring(numPos);
        switch (num) {
            case "inf" -> {
                return Optional.of(new TokenKind.FloatKind.Inf(negative));
            }
            case "nan" -> {
                return Optional.of(new TokenKind.FloatKind.Nan(negative));
            }
            case String ss when ss.startsWith("nan:0x") -> {
                // skip underscores and only take while char.digit is hexaDecimal
                AtomicBoolean hasUnderscores = new AtomicBoolean(false);
                IntPredicate predicate = (c) -> {
                    if (c == '_') {
                        hasUnderscores.set(true);
                        return true;
                    }
                    return Character.digit(c, 16) != -1;
                };
                if (ss.chars().skip(6).dropWhile(predicate).limit(1).findFirst().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(new TokenKind.FloatKind.NanVal(negative, hasUnderscores.get()));
            }
            default -> {
                boolean isHex = num.startsWith("0x");
                AtomicBoolean hasUnderscores = new AtomicBoolean(false);
                var stream = num.chars().skip((isHex) ? 2 : 0);
                IntPredicate predicate =  (c) -> {
                    if (c == '_') {
                        hasUnderscores.set(true);
                        return true;
                    }
                    return ((isHex) && Character.digit(c, 16) != -1) || Character.isDigit(c);
                };
                var remaining = stream.dropWhile(predicate).toArray();
                if (remaining == null || remaining.length == 0) {
                    return Optional.of(new TokenKind.IntKind(Optional.ofNullable(st), hasUnderscores.get(), isHex));
                }
                if (remaining[0] == '.') {
                    Arrays.stream(remaining).skip(1);
                }
                throw new IllegalStateException("Unexpected value: " + num);
            }
        }
    }

    Result<Optional<TokenKind>, LexError> parseKind(AtomicInteger pos) {
        // TODO
        int start = pos.get();
        char c = cs[start];

        switch (c) {
            case '(' -> {
                switch (get(start+1, 1)){
                    case null -> {
                        return new Result.Ok<>(Optional.of(TokenKind.TokenType.LParen));
                    }
                    case char[] cs when cs.length == 1 -> {}
                    default -> throw new IllegalStateException("Unexpected value: " + get(start + 1, 1));
                }
            }
        }
        return new Result.Error<>(new LexError.InvalidDigit('a'));
    }

    public Result<TokenKind, LexError> parseKindInt(AtomicInteger pos) {
        int i = pos.get();
        while (i < cs.length) {
            char c = cs[i];
            i++;
            switch (c) {
                case '(' -> {
                    System.out.println("Paren start");
                    if (i < cs.length && cs[i] == ';') {
                        System.out.println("Block comment start");
                        int lvl = 1;
                        int j = i+1;
                        while (j < cs.length) {
                            char cj = cs[j];
                            j++;
                            switch (cj) {
                                case '(' -> {
                                    if (j < cs.length && cs[j] == ';') {
                                        lvl += 1;
                                    }
                                }
                                case ';' -> {
                                    if (j < cs.length && cs[j] == ')') {
                                        lvl -= 1;
                                        if (lvl == 0) {
                                            pos.set(j+1);
                                            return new Result.Ok<>(TokenKind.TokenType.BlockComment);
                                        }
                                    }
                                }
                                default -> {}
                            }
                        }
                        return new Result.Error<>(new LexError.DanglingBlockComment());
                    }
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.LParen);
                }
                case ')' -> {
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.RParen);
                }
                case ' ', '\t', '\r', '\n' -> {
                    int nws = skipWhitespace(pos);
                    System.out.println("WS_SKIP: "+ nws);
                    return new Result.Ok<>(TokenKind.TokenType.Whitespace);
                }
                case ';' -> {
                    if (i < cs.length && cs[i] == ';') {
                        String lineComment = splitUntil(pos, '\n');
                        i = pos.get();
                        System.out.println("LINE_COMMENT: " + lineComment);
                        return new Result.Ok<>(TokenKind.TokenType.LineComment);
                    }
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.Reserved);
                }
                case ',', '[', ']', '{', '}' -> {
                    pos.set(i);
                    return new Result.Ok<>(TokenKind.TokenType.Reserved);
                }
                default -> {
                    if (ID_CHARS.contains(c) || c == '"') {
                        // TODO
                        pos.set(i);
                        var res = parseReserved(pos);
                        if (res.isError() && res.err().isPresent()) {
                            return new Result.Error<>(res.err().get());
                        }
                        var rk = res.ok().get();
                        switch (rk.getKey()) {
                            case IdChars -> {
                                // TODO classifyNum
                                if (c == '$' && rk.getValue().length() > 1) {
                                    return new Result.Ok<>(TokenKind.TokenType.Id);
                                }
                                if (c >= 'a' && c <= 'z') {
                                    return new Result.Ok<>(TokenKind.TokenType.Keyword);
                                }
                            }
                            case String -> {
                                return new Result.Ok<>(TokenKind.TokenType.String);
                            }
                            default -> {
                                // Just a Reserved token
                                return new Result.Ok<>(TokenKind.TokenType.Reserved);
                            }
                        }
                    } else {
                        // TODO
                        return new Result.Error<>(new LexError.Unexpected(c));
                    }
                }
            }
        }
        return new Result.Ok<>(TokenKind.TokenType.EOF);
    }

    public static void main(String[] args) {
        String prog = """
                ;; Sample line comment
                ((; (;Some comments ;) LOL;) LO
                """;
        var lexer = new Lexer(prog);
        AtomicInteger pos = new AtomicInteger(0);
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        System.out.println(lexer.parseKindInt(pos));
        // System.out.printf("ARR: %s%n", Arrays.toString(lexer.get(pos.get(), 2)));
        for (char c : prog.toCharArray()) {
            System.out.println( c + ": " + ID_CHARS.contains(c));
        }
        AtomicInteger pos2 = new AtomicInteger(0);
        System.out.println(lexer.splitUntil(pos2, 'S'));
        System.out.printf("SPLIT_UNTIL: %d%n", pos.get());
    }
}
