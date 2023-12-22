package rrampage.wasp.instructions;

public sealed interface SegmentInstruction extends Instruction {
    record MemoryInit(int dataSegmentIndex, int memoryIndex) implements SegmentInstruction {public String opCode() {return "mem.init";}}
    record DataDrop(int dataSegmentIndex) implements SegmentInstruction {public String opCode() {return "data.drop";}}
    record MemoryCopy(int srcMemoryIndex, int dstMemoryIndex) implements SegmentInstruction {public String opCode() {return "mem.copy";}}
    record MemoryFill(int memoryIndex) implements SegmentInstruction {public String opCode() {return "mem.fill";}}
}
