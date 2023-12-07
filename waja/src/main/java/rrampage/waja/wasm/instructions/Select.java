package rrampage.waja.wasm.instructions;

public record Select() implements Instruction {
    public String opCode() {
        return "select";
    }
}
