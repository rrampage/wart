package rrampage.waja.wasm;

public sealed interface Instruction {
    String opCode();
}

sealed interface ConstInstruction extends Instruction {}
record IntConst(int val) implements ConstInstruction { public String opCode() {return "i32.const";}}
record LongConst(long val) implements ConstInstruction { public String opCode() {return "i64.const";}}

record FloatConst(float val) implements ConstInstruction { public String opCode() {return "f32.const";}}

record DoubleConst(double val) implements ConstInstruction { public String opCode() {return "f64.const";}}

enum DoubleBinaryInstruction implements Instruction {
    F64_ADD    ("f64.add"),
    F64_SUB    ("f64.sub"),
    F64_MUL    ("f64.mul"),
    F64_DIV    ("f64.div"),
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
    public String opCode() { return opCode;}
}

enum FloatBinaryInstruction implements Instruction {
    F32_ADD    ("f32.add"),
    F32_SUB    ("f32.sub"),
    F32_MUL    ("f32.mul"),
    F32_DIV    ("f32.div"),
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
    public String opCode() { return opCode;}
}

enum IntBinaryInstruction implements Instruction {
    I32_ADD    ("i32.add"),
    I32_SUB    ("i32.sub"),
    I32_MUL    ("i32.mul"),
    I32_DIV_S    ("i32.div_s"),
    I32_REM_S    ("i32.rem_s"),
    I32_DIV_U    ("i32.div_u"),
    I32_REM_U    ("i32.rem_u"),
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
    I32_MAX("i32.max"),
    I32_MIN("i32.min"),
    I32_AND("i32.and"),
    I32_OR("i32.or"),
    I32_XOR("i32.xor"),
    ;
    private final String opCode;
    IntBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

enum LongBinaryInstruction implements Instruction {
    I64_ADD    ("i64.add"),
    I64_SUB    ("i64.sub"),
    I64_MUL    ("i64.mul"),
    I64_DIV_S    ("i64.div_s"),
    I64_DIV_U    ("i64.div_u"),
    I64_REM_S    ("i64.rem_s"),
    I64_REM_U    ("i64.rem_u"),
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
    I64_MAX("i64.max"),
    I64_MIN("i64.min"),
    I64_AND("i64.and"),
    I64_OR("i64.or"),
    I64_XOR("i64.xor"),
    ;
    private final String opCode;
    LongBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

enum UnaryInstruction implements Instruction {
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
    public String opCode() { return opCode;}
}

enum NullaryInstruction implements Instruction {
    NOP("nop"),
    MEMORY_SIZE("memory.size"),
    ;
    private final String opCode;
    NullaryInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

enum StoreInstruction implements Instruction {
    I32_STORE("i32.store"),
    I64_STORE("i64.store"),
    F32_STORE("f32.store"),
    F64_STORE("f64.store"),
    ;
    private final String opCode;
    StoreInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

enum LoadInstruction implements Instruction {
    // TODO: Implement alignment and offset later (memarg)
    I32_LOAD("i32.load"),
    I64_LOAD("i64.load"),
    F32_LOAD("f32.load"),
    F64_LOAD("f64.load"),
    ;
    private final String opCode;
    LoadInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

sealed interface FunctionInstruction extends Instruction {}

record Call(int val) implements FunctionInstruction { public String opCode() {return "call";}}
record LocalGet(int val) implements FunctionInstruction { public String opCode() {return "local.get";}}
record LocalSet(int val) implements FunctionInstruction { public String opCode() {return "local.set";}}
record LocalTee(int val) implements FunctionInstruction { public String opCode() {return "local.tee";}}

sealed interface GlobalInstruction extends Instruction {}
record GlobalGet(int val) implements GlobalInstruction { public String opCode() {return "global.get";}}
record GlobalSet(int val) implements GlobalInstruction { public String opCode() {return "global.set";}}

record Select(long t1, long t2, int cmp) implements Instruction {public String opCode() {return "select";}}