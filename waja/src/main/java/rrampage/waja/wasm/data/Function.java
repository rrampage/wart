package rrampage.waja.wasm.data;

import rrampage.waja.wasm.Instruction;

public record Function(FunctionType type, DataType[] locals, Instruction[] code) {
    public boolean isVoidReturn() {
        return type().isVoidReturn();
    }
    public int numParams() {
        return type().numParams();
    }

    public int numLocals() {
        return locals == null ? 0 : locals.length;
    }

    public String toString() {
        return type().toString();
    }
}
