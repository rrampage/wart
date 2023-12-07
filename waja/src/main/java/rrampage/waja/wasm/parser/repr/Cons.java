package rrampage.waja.wasm.parser.repr;

public sealed interface Cons permits ConsAtom, ConsList {
    String toString();
}
