package rrampage.waja.wasm.data;

public enum DataType {
    I32,
    I64,
    F32,
    F64;

    @Override
    public String toString() {
        return this.name();
    }
}