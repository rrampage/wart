package rrampage.wasp.instructions;

public sealed interface Instruction permits ConstInstruction, ControlFlowInstruction, DoubleBinaryInstruction, FloatBinaryInstruction, FunctionInstruction, GlobalInstruction, IntBinaryInstruction, LoadInstruction, LongBinaryInstruction, SegmentInstruction, NullaryInstruction, Select, StoreInstruction, UnaryInstruction {
    String opCode();
}

