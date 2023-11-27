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
    ;
    private final String opCode;
    LongBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}

enum UnaryInstruction implements Instruction {
    DROP("drop"),
    I32_EQZ("i32.eqz"),
    I32_POPCNT("i32.popcnt"),
    I64_EQZ("i64.eqz"),
    I64_POPCNT("i64.popcnt"),
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
    F64_CEIL("f64.ceil"),
    F64_FLOOR("f64.floor"),
    F64_TRUNC("f64.trunc"),
    ;
    private final String opCode;
    UnaryInstruction(String opCode) {
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