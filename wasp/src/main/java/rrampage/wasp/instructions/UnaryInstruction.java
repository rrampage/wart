package rrampage.wasp.instructions;

public enum UnaryInstruction implements Instruction {
    DROP("drop"),
    MEMORY_GROW("memory.grow"),
    I32_EQZ("i32.eqz"),
    I32_POPCNT("i32.popcnt"),
    I32_WRAP_I64("i32.wrap_i64"),
    I32_TRUNC_F32_S("i32.trunc_f32_s"),
    I32_TRUNC_F32_U("i32.trunc_f32_u"),
    I32_TRUNC_F64_S("i32.trunc_f64_s"),
    I32_TRUNC_F64_U("i32.trunc_f64_u"),
    I64_EQZ("i64.eqz"),
    I64_POPCNT("i64.popcnt"),
    I64_EXTEND_I32_S("i64.extend_i32_s"),
    I64_EXTEND_I32_U("i64.extend_i32_u"),
    I64_TRUNC_F32_S("i64.trunc_f32_s"),
    I64_TRUNC_F32_U("i64.trunc_f32_u"),
    I64_TRUNC_F64_S("i64.trunc_f64_s"),
    I64_TRUNC_F64_U("i64.trunc_f64_u"),
    I32_CTZ("i32.ctz"),
    I32_CLZ("i32.clz"),
    I64_CTZ("i64.ctz"),
    I64_CLZ("i64.clz"),
    F32_NEG("f32.neg"),
    F64_NEG("f64.neg"),
    F32_ABS("f32.abs"),
    F64_ABS("f64.abs"),
    F32_CEIL("f32.ceil"),
    F32_FLOOR("f32.floor"),
    F32_TRUNC("f32.trunc"),
    F32_DEMOTE_F64("f32.demote_f64"),
    F32_CONVERT_I32_S("f32.convert_i32_s"),
    F32_CONVERT_I32_U("f32.convert_i32_u"),
    F32_CONVERT_I64_S("f32.convert_i64_s"),
    F32_CONVERT_I64_U("f32.convert_i64_u"),
    F32_NEAREST("f32.nearest"),
    F32_SQRT("f32.sqrt"),
    F64_CEIL("f64.ceil"),
    F64_FLOOR("f64.floor"),
    F64_TRUNC("f64.trunc"),
    F64_PROMOTE_F32("f64.promote_f32"),
    F64_CONVERT_I32_S("f64.convert_i32_s"),
    F64_CONVERT_I32_U("f64.convert_i32_u"),
    F64_CONVERT_I64_S("f64.convert_i64_s"),
    F64_CONVERT_I64_U("f64.convert_i64_u"),
    F64_NEAREST("f64.nearest"),
    F64_SQRT("f64.sqrt"),
    I32_REINTERPRET_F32("i32.reinterpret_f32"),
    F32_REINTERPRET_I32("f32.reinterpret_i32"),
    I64_REINTERPRET_F64("i64.reinterpret_f64"),
    F64_REINTERPRET_I64("f64.reinterpret_i64"),
    ;
    private final String opCode;

    UnaryInstruction(String opCode) {
        this.opCode = opCode;
    }

    public String opCode() {
        return opCode;
    }
}
