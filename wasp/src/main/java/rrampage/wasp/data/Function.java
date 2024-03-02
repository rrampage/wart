package rrampage.wasp.data;

import rrampage.wasp.instructions.ControlFlowInstruction;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.FunctionInstruction;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public record Function(String name, FunctionType type, ValueType[] locals, Instruction[] code) {
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
            throw new RuntimeException(String.format("CREATE_IMPORT_FUNCTION: Invalid type %s supplied for Function %s. Required type: %s", func.type(), name, type));
        }
        return new Function(name, type, null, new Instruction[]{new FunctionInstruction.CallJava(type, func)});
    }

    public static Function createStartFunction(String name, Instruction[] code) {
        return new Function(name, new FunctionType(null, null), null, code);
    }

    public static Function rename(String name, Function f) {
        return new Function(name, f.type, f.locals, f.code);
    }
}
