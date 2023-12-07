package rrampage.waja.wasm.instructions;

public sealed interface GlobalInstruction extends Instruction {
    record GlobalGet(int val) implements GlobalInstruction { public String opCode() {return "global.get";}}
    record GlobalSet(int val) implements GlobalInstruction { public String opCode() {return "global.set";}}
}
