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
    I32_EQ("i32.eq"),
    I32_NE("i32.ne"),
    I32_GT_S("i32.gt_s"),
    I32_LT_S("i32.lt_s"),
    I32_GE_S("i32.ge"),
    I32_LE_S("i32.eq"),
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
    I64_EQ("i64.eq"),
    I64_NE("i64.ne"),
    I64_GT("i64.gt"),
    I64_LT("i64.lt"),
    I64_GE("i64.ge"),
    I64_LE("i64.eq"),
    I64_MAX("i64.max"),
    I64_MIN("i64.min"),
    ;
    private final String opCode;
    LongBinaryInstruction(String opCode) {
        this.opCode = opCode;
    }
    public String opCode() { return opCode;}
}