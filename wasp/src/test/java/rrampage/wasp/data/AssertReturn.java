package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

import java.util.Arrays;

public record AssertReturn(String function, ConstInstruction[] args, ConstInstruction[] expected) {
    public String toString() { return String.format("%s: Args: %s Expected: %s", function, Arrays.toString(args), Arrays.toString(expected));}
}
