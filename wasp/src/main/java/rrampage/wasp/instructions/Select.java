package rrampage.wasp.instructions;

public record Select() implements Instruction {
    public String opCode() {
        return "select";
    }
}
