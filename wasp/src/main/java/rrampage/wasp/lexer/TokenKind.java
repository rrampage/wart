package rrampage.wasp.lexer;

import java.util.Optional;

/**
 * TokenKind enum, adapted from <a href="https://github.com/bytecodealliance/wasm-tools/blob/main/crates/wast/src/lexer.rs">wasm-tools/wast</a>
 */
public sealed interface TokenKind {
    enum TokenType implements TokenKind {
        // A line comment, preceded with `;;`
        LineComment,

        // A block comment, surrounded by `(;` and `;)`. Note that these can be nested.
        BlockComment,

        /// A fragment of source that represents whitespace.
        Whitespace,

        // A left-parenthesis, including the source text for where it comes from.
        LParen,
        // A right-parenthesis, including the source text for where it comes from.
        RParen,

        // A string literal, which is actually a list of bytes.
        String,

        // An identifier (like `$foo`).
        // All identifiers start with `$` and the payload here is the original source text.
        Id,

        // A keyword, or something that starts with an alphabetic character.
        // The payload here is the original source text.
        Keyword,

        // A reserved series of `idchar` symbols. Unknown what this is meant to be
        // used for, you'll probably generate an error about an unexpected token.
        Reserved,

        EOF,
    }

    record IntKind(Optional<SignToken> sign, boolean hasUnderscores, boolean hex) implements TokenKind {}
    sealed interface FloatKind extends TokenKind {
        record Inf(boolean negative) implements FloatKind {}
        record Nan(boolean negative) implements FloatKind {}
        record NanVal(boolean negative, boolean hasUnderscores) implements FloatKind {}
        record Normal(boolean hasUnderscores, boolean hex) implements FloatKind {}
    }

    enum ReservedKind {
        String,
        IdChars,
        Reserved;
    }
}
