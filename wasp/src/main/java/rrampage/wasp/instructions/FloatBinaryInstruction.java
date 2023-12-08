package rrampage.wasp.instructions;

public enum FloatBinaryInstruction implements Instruction {
    F32_ADD("f32.add"),
    F32_SUB("f32.sub"),
    F32_MUL("f32.mul"),
    F32_DIV("f32.div"),
    F32_EQ("f32.eq"),
    F32_NE("f32.ne"),
    F32_GT("f32.gt"),
    F32_LT("f32.lt"),
    F32_GE("f32.ge"),
    F32_LE("f32.eq"),
    F32_MAX("f32.max"),
    F32_MIN("f32.min"),
    F32_COPY_SIGN("f32.copysign"),
    ;
    private final String opCode;

    FloatBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
