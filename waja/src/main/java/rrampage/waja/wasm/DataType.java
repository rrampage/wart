package rrampage.waja.wasm;

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