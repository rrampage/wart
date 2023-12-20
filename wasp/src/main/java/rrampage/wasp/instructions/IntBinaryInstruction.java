package rrampage.wasp.instructions;

public enum IntBinaryInstruction implements Instruction {
    I32_ADD("i32.add"),
    I32_SUB("i32.sub"),
    I32_MUL("i32.mul"),
    I32_DIV_S("i32.div_s"),
    I32_REM_S("i32.rem_s"),
    I32_DIV_U("i32.div_u"),
    I32_REM_U("i32.rem_u"),
    I32_EQ("i32.eq"),
    I32_NE("i32.ne"),
    I32_GT_S("i32.gt_s"),
    I32_LT_S("i32.lt_s"),
    I32_GE_S("i32.ge_s"),
    I32_LE_S("i32.le_s"),
    I32_GT_U("i32.gt_u"),
    I32_LT_U("i32.lt_u"),
    I32_GE_U("i32.ge_u"),
    I32_LE_U("i32.le_u"),
    I32_AND("i32.and"),
    I32_OR("i32.or"),
    I32_XOR("i32.xor"),
    I32_SHL("i32.shl"),
    I32_SHR_S("i32.shr_s"),
    I32_SHR_U("i32.shr_u"),
    I32_ROTL("i32.rotl"),
    I32_ROTR("i32.rotr"),
    ;
    private final String opCode;

    IntBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
