package rrampage.wasp.parser.repr;

public sealed interface Cons permits ConsAtom, ConsList {
    String toString();
}
