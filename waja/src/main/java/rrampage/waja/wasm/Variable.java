package rrampage.waja.wasm;

import static rrampage.waja.utils.ConversionUtils.*;

public sealed interface Variable {
    DataType getType();
    public static Variable newVariable(DataType dataType, long val) {
        return switch (dataType) {
            case I32 -> new I32Variable(longToInt(val));
            case I64 -> new I64Variable(val);
            case F32 -> new F32Variable(longToFloat(val));
            case F64 -> new F64Variable(longToDouble(val));
        };
    }
}

record I32Variable(int val) implements Variable { public DataType getType() {return DataType.I32;}}
record I64Variable(long val) implements Variable {public DataType getType() {return DataType.I64;}}
record F32Variable(float val) implements Variable {public DataType getType() {return DataType.F32;}}
record F64Variable(double val) implements Variable {public DataType getType() {return DataType.F64;}}
