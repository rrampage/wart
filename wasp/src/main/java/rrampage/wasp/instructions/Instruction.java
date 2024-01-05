package rrampage.wasp.instructions;

public sealed interface Instruction permits
        ConstExpression, ConstInstruction, ControlFlowInstruction, DoubleBinaryInstruction,
        FloatBinaryInstruction, FunctionInstruction, GlobalInstruction, IntBinaryInstruction,
        LoadInstruction, LongBinaryInstruction, NullaryInstruction, RefTypeInstruction,
        SegmentInstruction, Select, StoreInstruction, UnaryInstruction {
    String opCode();
    static Instruction[] of(Instruction ... args) {return args;}
}

