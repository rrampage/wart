package rrampage.wasp.instructions;

public sealed interface ConstInstruction extends Instruction {
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
