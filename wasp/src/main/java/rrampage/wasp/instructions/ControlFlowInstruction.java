package rrampage.wasp.instructions;

import rrampage.wasp.data.FunctionType;

import java.util.Arrays;

public sealed interface ControlFlowInstruction extends Instruction {
    record Block(int label, FunctionType type, Instruction[] code) implements ControlFlowInstruction {
        public String opCode() {return "block";}
        public String toString() {return String.format("BLOCK {{Type: %s}} Label: %d [[Code: %s]]", type, label, Arrays.toString(code));}
    }
    record Loop(int label, FunctionType type, Instruction[] code) implements ControlFlowInstruction {
        public String opCode() {return "loop";}
        public String toString() {return String.format("LOOP {{Type: %s}} Label: %d [[Code: %s]]", type, label, Arrays.toString(code));}
    }
    record Branch(int label) implements ControlFlowInstruction {public String opCode() {return "br";}}
    record BranchIf(int label) implements ControlFlowInstruction {public String opCode() {return "br_if";}}
    record BranchTable(int[] labels, int defaultLabel) implements ControlFlowInstruction {public String opCode() {return "br_table";}}
    record If(int label, FunctionType type, Instruction[] ifBlock) implements ControlFlowInstruction {
        public String opCode() {return "if";}
        public String toString() {return String.format("IF {{Type: %s}} Label: %d [[Code: %s]]", type, label, Arrays.toString(ifBlock));}
    }
    record IfElse(int label, FunctionType type, Instruction[] ifBlock, Instruction[] elseBlock) implements ControlFlowInstruction {
        public String opCode() {return "if";}
        public String toString() {return String.format("IF_ELSE {{Type: %s}} Label: %d [[IfBlock: %s]] [[ElseBlock: %s]]", type, label, Arrays.toString(ifBlock), Arrays.toString(elseBlock));}
    }
    record End() implements ControlFlowInstruction {public String opCode() {return "end";}}
    record Else() implements ControlFlowInstruction {public String opCode() {return "else";}}
}
