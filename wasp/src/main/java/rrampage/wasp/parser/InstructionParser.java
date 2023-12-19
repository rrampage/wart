package rrampage.wasp.parser;

import rrampage.wasp.instructions.*;
import rrampage.wasp.utils.Leb128;
import static rrampage.wasp.instructions.ByteCodeConstants.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class InstructionParser {
    public static Instruction[] parse(ByteBuffer in, int numBytes) {
        int startPos = in.position();
        int i = 0;
        ArrayList<Instruction> insList =  new ArrayList<>(numBytes/2);
        loopBody:
        while (i < numBytes) {
            int b = Byte.toUnsignedInt(in.get()); // get bytecode of instruction
            switch (b) {
                // Const instructions
                case CONST_INT, CONST_LONG, CONST_FLOAT, CONST_DOUBLE -> insList.add(parseConstantInstruction(b, in));
                case GLOBAL_GET, GLOBAL_SET -> insList.add(parseGlobalInstruction(b, in));
                case LOCAL_GET, LOCAL_SET, LOCAL_TEE, FUNC_CALL, FUNC_CALL_INDIRECT, FUNC_RETURN -> insList.add(parseFunctionInstruction(b, in));
                case LOAD_I32, LOAD8_I32_S, LOAD8_I32_U, LOAD16_I32_S, LOAD16_I32_U, LOAD_F32, LOAD_F64,
                        LOAD_I64, LOAD8_I64_S, LOAD8_I64_U, LOAD16_I64_S,
                        LOAD16_I64_U, LOAD32_I64_S, LOAD32_I64_U -> insList.add(parseLoadInstruction(b, in));
                case STORE_I32, STORE_I64, STORE_F32, STORE_F64, STORE8_I32, STORE16_I32,
                        STORE8_I64, STORE16_I64, STORE32_I64 -> insList.add(parseStoreInstruction(b, in));
                default -> {break loopBody;}
            }
            i += in.position() - startPos;

        }
        return insList.toArray(Instruction[]::new);
    }

    private static StoreInstruction parseStoreInstruction(int byteCode, ByteBuffer in) {
        System.out.printf("Parsing Store instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case STORE_I32 -> new StoreInstruction.I32Store((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE_I64 -> new StoreInstruction.I64Store((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE_F32 -> new StoreInstruction.F32Store((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE_F64 -> new StoreInstruction.F64Store((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE8_I32 -> new StoreInstruction.I32Store8((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE16_I32 -> new StoreInstruction.I32Store16((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE8_I64 -> new StoreInstruction.I64Store8((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE16_I64 -> new StoreInstruction.I64Store16((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case STORE32_I64 -> new StoreInstruction.I64Store32((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            default -> throw new RuntimeException("Unexpected bytecode for store instruction: " + byteCode);
        };
    }

    private static LoadInstruction parseLoadInstruction(int byteCode, ByteBuffer in) {
        System.out.printf("Parsing Load instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case LOAD_I32 -> new LoadInstruction.I32Load((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD8_I32_S -> new LoadInstruction.I32Load8S((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD8_I32_U -> new LoadInstruction.I32Load8U((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD16_I32_S -> new LoadInstruction.I32Load16S((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD16_I32_U -> new LoadInstruction.I32Load16U((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD_I64 -> new LoadInstruction.I64Load((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD8_I64_S -> new LoadInstruction.I64Load8S((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD8_I64_U -> new LoadInstruction.I64Load8U((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD16_I64_S -> new LoadInstruction.I64Load16S((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD16_I64_U -> new LoadInstruction.I64Load16U((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD32_I64_S -> new LoadInstruction.I64Load32S((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD32_I64_U -> new LoadInstruction.I64Load32U((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD_F32 -> new LoadInstruction.F32Load((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case LOAD_F64 -> new LoadInstruction.F64Load((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            default -> throw new RuntimeException("Unexpected bytecode for load instruction: " + byteCode);
        };
    }

    private static ConstInstruction parseConstantInstruction(int byteCode, ByteBuffer in) {
        System.out.printf("Parsing Constant instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case CONST_INT -> new ConstInstruction.IntConst((int) Leb128.readSigned(in));
            case CONST_LONG -> new ConstInstruction.LongConst(Leb128.readSigned(in));
            case CONST_FLOAT -> new ConstInstruction.FloatConst(in.getFloat());
            case CONST_DOUBLE -> new ConstInstruction.DoubleConst(in.getDouble());
            default -> throw new RuntimeException("Unexpected bytecode for constant instruction: " + byteCode);
        };
    }

    private static FunctionInstruction parseFunctionInstruction(int byteCode, ByteBuffer in) {
        System.out.printf("Parsing Function instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case LOCAL_GET -> new FunctionInstruction.LocalGet((int) Leb128.readUnsigned(in));
            case LOCAL_SET -> new FunctionInstruction.LocalSet((int) Leb128.readUnsigned(in));
            case LOCAL_TEE -> new FunctionInstruction.LocalTee((int) Leb128.readUnsigned(in));
            case FUNC_RETURN -> new FunctionInstruction.Return();
            case FUNC_CALL -> new FunctionInstruction.Call((int) Leb128.readUnsigned(in));
            case FUNC_CALL_INDIRECT -> new FunctionInstruction.CallIndirect((int) Leb128.readUnsigned(in), in.get());
            default -> throw new RuntimeException("Unexpected bytecode for global instruction: " + byteCode);
        };
    }

    private static GlobalInstruction parseGlobalInstruction(int byteCode, ByteBuffer in) {
        System.out.printf("Parsing function instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case GLOBAL_GET -> new GlobalInstruction.GlobalGet((int) Leb128.readUnsigned(in));
            case GLOBAL_SET -> new GlobalInstruction.GlobalSet((int) Leb128.readUnsigned(in));
            default -> throw new RuntimeException("Unexpected bytecode for function instruction: " + byteCode);
        };
    }
}
