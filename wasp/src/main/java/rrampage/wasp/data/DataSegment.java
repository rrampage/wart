package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstInstruction;

public sealed interface DataSegment {
    record ActiveDataSegment(int memoryIndex, ConstInstruction.IntConst offset, byte[] data) implements DataSegment {}
    record PassiveDataSegment(byte[] data) implements DataSegment {}

    byte[] data();
}
