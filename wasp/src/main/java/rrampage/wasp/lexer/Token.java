package rrampage.wasp.lexer;
public record Token(TokenKind kind, int offset, int len) {}