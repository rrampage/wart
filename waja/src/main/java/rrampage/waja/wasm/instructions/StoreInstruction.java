package rrampage.waja.wasm.instructions;

public sealed interface StoreInstruction extends Instruction {
    int offset();

    int align();
    record I32Store(int align, int offset) implements StoreInstruction {public String opCode() {return "i32.store";}}
    record I32Store8(int align, int offset) implements StoreInstruction {public String opCode() {return "i32.store8";}}
    record I32Store16(int align, int offset) implements StoreInstruction {public String opCode() {return "i32.store16";}}
    record I64Store(int align, int offset) implements StoreInstruction {public String opCode() {return "i64.store";}}
    record I64Store8(int align, int offset) implements StoreInstruction {public String opCode() {return "i64.store8";}}
    record I64Store16(int align, int offset) implements StoreInstruction {public String opCode() {return "i64.store16";}}
    record I64Store32(int align, int offset) implements StoreInstruction {public String opCode() {return "i64.store32";}}
    record F32Store(int align, int offset) implements StoreInstruction {public String opCode() {return "f32.store";}}
    record F64Store(int align, int offset) implements StoreInstruction {public String opCode() {return "f64.store";}}
}
