package rrampage.waja.wasm;

public record Function(DataType[] paramTypes, DataType[] locals, DataType[] returnTypes, Instruction[] code) {
    public boolean isVoidReturn() {
        return returnTypes == null || returnTypes.length == 0;
    }
    public int numParams() {
        return paramTypes == null ? 0 : paramTypes.length;
    }

    public int numLocals() {
        return locals == null ? 0 : locals.length;
    }
}
