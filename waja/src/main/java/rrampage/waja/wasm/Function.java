package rrampage.waja.wasm;

import java.util.Arrays;

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

    @Override
    public String toString() {
        return String.format("Function with params: %s, return: %s",
                (numParams() == 0) ? 0 : Arrays.toString(paramTypes),
                (isVoidReturn()) ? "void" : Arrays.toString(returnTypes)
        );
    }
}
