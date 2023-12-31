package rrampage.wasp.instructions;

public sealed interface ConstInstruction extends Instruction {
    record IntConst(int val) implements ConstInstruction, ConstExpression { public String opCode() {return "i32.const";}}
    record LongConst(long val) implements ConstInstruction { public String opCode() {return "i64.const";}}
    record FloatConst(float val) implements ConstInstruction { public String opCode() {return "f32.const";}}
    record DoubleConst(double val) implements ConstInstruction { public String opCode() {return "f64.const";}}
    static ConstInstruction[] of(ConstInstruction ... args) {return args;}
}
