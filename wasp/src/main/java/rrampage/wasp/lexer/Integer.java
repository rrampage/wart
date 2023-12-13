package rrampage.wasp.lexer;

import java.util.Optional;

public record Integer(Optional<SignToken> sign, String val, boolean hex) {
}
