package rrampage.wasp.data;

import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.FunctionInstruction;

import java.lang.invoke.MethodHandle;

public record Function(String name, FunctionType type, DataType[] locals, Instruction[] code) {
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
        return String.format("Function: %s Type: %s", name, type);
    }

    public static Function createStubFunction(String name, FunctionType type) {
        return new Function(name, type, null, new Instruction[]{});
    }

    public static Function createImportFunction(String name, FunctionType type, MethodHandle func) {
        // Defined type signature must match
        if (!func.type().equals(FunctionType.getMethodTypeFromFunctionType(type))) {
            // Can throw RuntimeException here??
            return null;
        }
        return new Function(name, type, null, new Instruction[]{new FunctionInstruction.CallJava(type, func)});
    }
}
