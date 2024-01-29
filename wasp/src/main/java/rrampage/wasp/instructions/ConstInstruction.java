package rrampage.wasp.instructions;

import rrampage.wasp.utils.MathUtils;

public sealed interface ConstInstruction extends Instruction {
    public static FloatConst NAN_ARITHMETIC_FLOAT = new FloatConst(Float.NaN);
    public static FloatConst NAN_CANONICAL_FLOAT = new FloatConst(MathUtils.FLOAT_CANONICAL_NAN);
    public static DoubleConst NAN_ARITHMETIC_DOUBLE = new DoubleConst(Double.NaN);
    public static DoubleConst NAN_CANONICAL_DOUBLE = new DoubleConst(MathUtils.DOUBLE_CANONICAL_NAN);
    record IntConst(int val) implements ConstInstruction, ConstExpression {
        public String opCode() {return "i32.const";}
        public String toString() {return view();}
    }
    record LongConst(long val) implements ConstInstruction, ConstExpression {
        public String opCode() {return "i64.const";}
        public String toString() {return view();}
    }
    record FloatConst(float val) implements ConstInstruction, ConstExpression {
        public String opCode() {return "f32.const";}
        public String toString() {return view();}
    }
    record DoubleConst(double val) implements ConstInstruction, ConstExpression {
        public String opCode() {return "f64.const";}
        public String toString() {return view();}
    }
    static ConstInstruction[] of(ConstInstruction ... args) {return args;}
    default String view() {
        return switch (this) {
            case DoubleConst i -> "f64:" + i.val();
            case FloatConst i -> "f32:" + i.val() + " raw: " + Integer.toUnsignedString(Float.floatToRawIntBits(i.val));
            case IntConst i -> "i32:" + i.val();
            case LongConst i -> "i64:" + i.val();
        };
    }
}
