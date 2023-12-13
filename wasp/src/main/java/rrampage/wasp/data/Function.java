package rrampage.wasp.data;

import rrampage.wasp.instructions.ControlFlowInstruction;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.instructions.FunctionInstruction;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

public record Function(String name, FunctionType type, DataType[] locals, Instruction[] code, int[] labels) {
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

    public static int getMaxLabelFromInstructions(Instruction[] code) {
        if (code == null || code.length == 0) {
            return 0;
        }
        int maxLabel = 0;
        for (Instruction i : code) {
            switch (i) {
                case ControlFlowInstruction.Loop l -> maxLabel = Math.max(maxLabel, Math.max(l.label(), getMaxLabelFromInstructions(l.code())));
                case ControlFlowInstruction.Block l -> maxLabel = Math.max(maxLabel, Math.max(l.label(), getMaxLabelFromInstructions(l.code())));
                default -> {}
            }
        }
        return maxLabel;
    }

    public static int[] getLabelsFromInstructions(Instruction[] code) {
        int maxLabel = getMaxLabelFromInstructions(code);
        int[] labels = new int[maxLabel+1];
        Arrays.fill(labels, -1);
        return labels;
    }

    public static Function createStubFunction(String name, FunctionType type) {
        return new Function(name, type, null, new Instruction[]{}, null);
    }

    public static Function createImportFunction(String name, FunctionType type, MethodHandle func) {
        // Defined type signature must match
        if (!func.type().equals(FunctionType.getMethodTypeFromFunctionType(type))) {
            // Can throw RuntimeException here??
            return null;
        }
        return new Function(name, type, null, new Instruction[]{new FunctionInstruction.CallJava(type, func)}, null);
    }
}
