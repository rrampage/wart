package rrampage.wasp.parser;

public sealed interface Cons permits ConsAtom, ConsList {
    String toString();
}
