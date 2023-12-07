package rrampage.waja.wasm.instructions;

public sealed interface ControlFlowInstruction extends Instruction {
    record Block(int label, Instruction[] code) implements ControlFlowInstruction {public String opCode() {return "block";}}
    record Loop(int label, Instruction[] code) implements ControlFlowInstruction {public String opCode() {return "loop";}}
    record Branch(int label) implements ControlFlowInstruction {public String opCode() {return "br";}}
    record BranchIf(int label) implements ControlFlowInstruction {public String opCode() {return "br_if";}}
    record If(Instruction[] ifBlock) implements ControlFlowInstruction {public String opCode() {return "if";}}
    record IfElse(Instruction[] ifBlock, Instruction[] elseBlock) implements ControlFlowInstruction {public String opCode() {return "if_else";}}
}
