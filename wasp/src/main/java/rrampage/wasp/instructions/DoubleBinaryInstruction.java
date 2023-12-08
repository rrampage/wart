package rrampage.wasp.instructions;

public enum DoubleBinaryInstruction implements Instruction {
    F64_ADD("f64.add"),
    F64_SUB("f64.sub"),
    F64_MUL("f64.mul"),
    F64_DIV("f64.div"),
    F64_EQ("f64.eq"),
    F64_NE("f64.ne"),
    F64_GT("f64.gt"),
    F64_LT("f64.lt"),
    F64_GE("f64.ge"),
    F64_LE("f64.eq"),
    F64_MAX("f64.max"),
    F64_MIN("f64.min"),
    F64_COPY_SIGN("f64.copysign"),
    ;
    private final String opCode;

    DoubleBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
