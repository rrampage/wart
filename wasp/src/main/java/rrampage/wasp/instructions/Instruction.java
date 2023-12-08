package rrampage.wasp.instructions;

import java.util.ArrayList;

public sealed interface Instruction permits ConstInstruction, ControlFlowInstruction, DoubleBinaryInstruction, FloatBinaryInstruction, FunctionInstruction, GlobalInstruction, IntBinaryInstruction, LoadInstruction, LongBinaryInstruction, NullaryInstruction, Select, StoreInstruction, UnaryInstruction {
    String opCode();
    static Instruction[] parse(String s) {
        return InstructionParser.parse(s);
    }
}

class InstructionParser {
    private static final String BLOCK_INS = "block";
    private static final String LOOP_INS = "loop";
    private static final String IF_INS = "if";
    private static final String ELSE_INS = "else";

    private static final String endIns = "end";
    private static Instruction parseSimple(String s) {
        // TODO
        return NullaryInstruction.NOP;
    }

    static Instruction[] parse(String s) {
        return parseInternal(s.split("\n"));
    }

    static Instruction[] parseInternal(String[] s) {
        // TODO
        ArrayList<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < s.length; i++) {
            String raw = s[i].trim();
            boolean isBlockIns = raw.startsWith(BLOCK_INS) || raw.startsWith(LOOP_INS);
            boolean isIfIns = raw.startsWith(IF_INS) || raw.startsWith(ELSE_INS);
        }
        return instructions.toArray(new Instruction[]{});
    }
}