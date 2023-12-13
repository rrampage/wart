package rrampage.wasp.lexer;

import java.util.Optional;

sealed public interface Float {
    record Nan(Optional<String> val, boolean negative) implements Float {
    }

    record Inf(boolean negative) implements Float {
    }

    record Val(boolean hex, String integral, Optional<String> decimal, Optional<String> exponent) implements Float {
    }
}
