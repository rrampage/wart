package rrampage.wasp.instructions;

public enum LongBinaryInstruction implements Instruction {
    I64_ADD("i64.add"),
    I64_SUB("i64.sub"),
    I64_MUL("i64.mul"),
    I64_DIV_S("i64.div_s"),
    I64_DIV_U("i64.div_u"),
    I64_REM_S("i64.rem_s"),
    I64_REM_U("i64.rem_u"),
    I64_EQ("i64.eq"),
    I64_NE("i64.ne"),
    I64_GT_S("i64.gt_s"),
    I64_LT_S("i64.lt_s"),
    I64_GE_S("i64.ge_s"),
    I64_LE_S("i64.le_s"),
    I64_GT_U("i64.gt_u"),
    I64_LT_U("i64.lt_u"),
    I64_GE_U("i64.ge_u"),
    I64_LE_U("i64.le_u"),
    I64_AND("i64.and"),
    I64_OR("i64.or"),
    I64_XOR("i64.xor"),
    I64_SHL("i64.shl"),
    I64_SHR_S("i64.shr_s"),
    I64_SHR_U("i64.shr_u"),
    I64_ROTL("i64.rotl"),
    I64_ROTR("i64.rotr"),
    ;
    private final String opCode;

    LongBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
