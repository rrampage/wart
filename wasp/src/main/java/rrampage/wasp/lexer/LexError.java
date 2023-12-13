package rrampage.wasp.lexer;

public sealed interface LexError {
    /// A dangling block comment was found with an unbalanced `(;` which was
    /// never terminated in the file.
    record DanglingBlockComment() implements LexError {}

    /// An unexpected character was encountered when generally parsing and
    /// looking for something else.
    record Unexpected(char c) implements LexError {}

    /// An invalid `char` in a string literal was found.
    record InvalidStringElement(char c) implements LexError {}

    /// An invalid string escape letter was found (the thing after the `\` in
    /// string literals)
    record InvalidStringEscape(char c) implements LexError {}

    /// An invalid hexadecimal digit was found.
    record InvalidHexDigit(char c) implements LexError {}

    /// An invalid base-10 digit was found.
    record InvalidDigit(char c) implements LexError {}

    /// Parsing expected `wanted` but ended up finding `found` instead where the
    /// two characters aren't the same.
    record Expected(char wanted, char found)  implements LexError {}

    /// We needed to parse more but EOF (or end of the string) was encountered.
    record UnexpectedEof() implements LexError {}

    /// A number failed to parse because it was too big to fit within the target
    /// type.
    record NumberTooBig() implements LexError {}

    /* An invalid unicode value was found in a `\\u{...}` escape in a string, only valid unicode scalars can be escaped that way. */
    record InvalidUnicodeValue(long val) implements LexError {}

    /// A lone underscore was found when parsing a number, since underscores
    /// should always be preceded and succeeded with a digit of some form.
    record LoneUnderscore() implements LexError {}

    /// A "confusing" unicode character is present in a comment or a string
    /// literal, such as a character that changes the direction text is
    /// typically displayed in editors. This could cause the human-read
    /// version to behave differently than the compiler-visible version, so
    /// these are simply rejected for now.
    record ConfusingUnicode(char c) implements LexError {}
}
