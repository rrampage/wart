package rrampage.wasp.instructions;

public sealed interface ConstInstruction extends Instruction {
    public static FloatConst NAN_ARITHMETIC_FLOAT = new FloatConst(Float.NaN);
    public static FloatConst NAN_CANONICAL_FLOAT = new FloatConst(Float.intBitsToFloat(0x10000000));
    public static DoubleConst NAN_ARITHMETIC_DOUBLE = new DoubleConst(Double.NaN);
    public static DoubleConst NAN_CANONICAL_DOUBLE = new DoubleConst(Double.longBitsToDouble(0x1000000000000000L));
    record IntConst(int val) implements ConstInstruction, ConstExpression {
        public String opCode() {return "i32.const";}
        public String toString() {return view();}
    }
    record LongConst(long val) implements ConstInstruction {
        public String opCode() {return "i64.const";}
        public String toString() {return view();}
    }
    record FloatConst(float val) implements ConstInstruction {
        public String opCode() {return "f32.const";}
        public String toString() {return view();}
    }
    record DoubleConst(double val) implements ConstInstruction {
        public String opCode() {return "f64.const";}
        public String toString() {return view();}
    }
    static ConstInstruction[] of(ConstInstruction ... args) {return args;}
    default String view() {
        return switch (this) {
            case DoubleConst i -> "f64:" + i.val();
            case FloatConst i -> "f32:" + i.val();
            case IntConst i -> "i32:" + i.val();
            case LongConst i -> "i64:" + i.val();
        };
    }
}
