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
                default -> {break loopBody;}
            }
            i += in.position() - startPos;

        }
        return insList.toArray(Instruction[]::new);
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
