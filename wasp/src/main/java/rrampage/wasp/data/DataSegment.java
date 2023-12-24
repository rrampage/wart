package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstExpression;

public sealed interface DataSegment {
    record ActiveDataSegment(int memoryIndex, ConstExpression offset, byte[] data) implements DataSegment {}
    record PassiveDataSegment(byte[] data) implements DataSegment {}
    byte[] data();
}
