package rrampage.wasp.instructions;

public enum NullaryInstruction implements Instruction {
    UNREACHABLE("unreachable"),
    NOP("nop"),
    MEMORY_SIZE("memory.size"),
    ;
    private final String opCode;

    NullaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
