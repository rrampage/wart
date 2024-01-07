package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

public record AssertReturn(String function, ConstInstruction[] args, ConstInstruction[] expected) {}
