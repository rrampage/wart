package rrampage.wasp.parser;

import java.util.List;
import java.util.stream.Collectors;

public record ConsList(List<Cons> val) implements Cons {
    public String toString() {
        return "(" + val.stream().map(Cons::toString).collect(Collectors.joining(" ")) + ")";
    }

    public boolean startsWith(String term) {
        return !val().isEmpty() && switch (val().get(0)) {
            case ConsAtom c -> c.val().startsWith(term);
            case ConsList c -> false;
        };
    }
}
