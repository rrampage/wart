package rrampage.wasp.instructions;

import rrampage.wasp.parser.InstructionParser;

public sealed interface Instruction permits ConstInstruction, ControlFlowInstruction, DoubleBinaryInstruction, FloatBinaryInstruction, FunctionInstruction, GlobalInstruction, IntBinaryInstruction, LoadInstruction, LongBinaryInstruction, NullaryInstruction, Select, StoreInstruction, UnaryInstruction {
    String opCode();
    static Instruction[] parse(String s) {
        return InstructionParser.parse(s);
    }
}

