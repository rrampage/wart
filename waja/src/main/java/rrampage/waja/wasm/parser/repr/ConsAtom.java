package rrampage.waja.wasm.parser.repr;

public record ConsAtom(String val) implements Cons {
    public String toString() {
        return val;
    }
}
