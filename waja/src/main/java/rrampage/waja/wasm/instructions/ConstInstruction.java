package rrampage.waja.wasm.instructions;

public sealed interface ConstInstruction extends Instruction {
    record IntConst(int val) implements ConstInstruction { public String opCode() {return "i32.const";}}
    record LongConst(long val) implements ConstInstruction { public String opCode() {return "i64.const";}}

    record FloatConst(float val) implements ConstInstruction { public String opCode() {return "f32.const";}}

    record DoubleConst(double val) implements ConstInstruction { public String opCode() {return "f64.const";}}
}
