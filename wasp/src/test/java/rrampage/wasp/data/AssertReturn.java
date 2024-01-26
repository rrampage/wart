package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

import java.util.Arrays;

public record AssertReturn(String function, String line, ConstInstruction[] args, ConstInstruction[] expected) {
    public String toString() { return String.format("%s: Line %s Args: %s Expected: %s", function, line, Arrays.toString(args), Arrays.toString(expected));}
}
