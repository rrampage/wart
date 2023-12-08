package rrampage.wasp.parser.repr;

public record ConsAtom(String val) implements Cons {
    public String toString() {
        return val;
    }
}
