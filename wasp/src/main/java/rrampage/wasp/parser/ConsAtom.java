package rrampage.wasp.parser;

public record ConsAtom(String val) implements Cons {
    public String toString() {
        return val;
    }
}
