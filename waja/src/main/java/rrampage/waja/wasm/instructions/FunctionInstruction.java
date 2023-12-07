package rrampage.waja.wasm.instructions;

import rrampage.waja.wasm.data.FunctionType;

import java.lang.invoke.MethodHandle;

public sealed interface FunctionInstruction extends Instruction {
    record Call(int val) implements FunctionInstruction { public String opCode() {return "call";}}
    record CallJava(FunctionType type, MethodHandle function) implements FunctionInstruction { public String opCode() {return "java";}}
    record LocalGet(int val) implements FunctionInstruction { public String opCode() {return "local.get";}}
    record LocalSet(int val) implements FunctionInstruction { public String opCode() {return "local.set";}}
    record LocalTee(int val) implements FunctionInstruction { public String opCode() {return "local.tee";}}
}
