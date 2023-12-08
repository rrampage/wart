package rrampage.wasp.instructions;

public sealed interface LoadInstruction extends Instruction {
    int offset();

    int align();
    record I32Load(int align, int offset) implements LoadInstruction {public String opCode() {return "i32.load";}}
    record I32Load8S(int align, int offset) implements LoadInstruction {public String opCode() {return "i32.load8_s";}}
    record I32Load8U(int align, int offset) implements LoadInstruction {public String opCode() {return "i32.load8_u";}}
    record I32Load16S(int align, int offset) implements LoadInstruction {public String opCode() {return "i32.load16_s";}}
    record I32Load16U(int align, int offset) implements LoadInstruction {public String opCode() {return "i32.load16_u";}}
    record I64Load(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load";}}
    record I64Load8S(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load8_s";}}
    record I64Load8U(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load8_u";}}
    record I64Load16S(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load16_s";}}
    record I64Load16U(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load16_u";}}
    record I64Load32S(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load32_s";}}
    record I64Load32U(int align, int offset) implements LoadInstruction {public String opCode() {return "i64.load32_u";}}
    record F32Load(int align, int offset) implements LoadInstruction {public String opCode() {return "f32.load";}}
    record F64Load(int align, int offset) implements LoadInstruction {public String opCode() {return "f64.load";}}
}
