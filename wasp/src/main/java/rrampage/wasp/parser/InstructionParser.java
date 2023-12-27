package rrampage.wasp.parser;

import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.instructions.*;
import rrampage.wasp.utils.Leb128;
import static rrampage.wasp.instructions.ByteCodeConstants.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class InstructionParser {
    public static Instruction[] parse(ByteBuffer in, int numBytes, FunctionType[] types) {
        int startPos = in.position();
        System.out.printf("PARSE_INSTRUCTION startPos: %d numBytes: %d\n", startPos, numBytes);
        ArrayList<Instruction> insList =  new ArrayList<>(numBytes/2);
        // bytesToParse is numBytes-1 as we are ignoring the last 0xb byte marking function end
        int bytesToParse = numBytes-1;
        AtomicInteger currLabel = new AtomicInteger(-1);
        while (in.position() - startPos < bytesToParse) {
            insList.add(parseInstruction(in, startPos, bytesToParse, types, currLabel));
        }
        return insList.toArray(Instruction[]::new);
    }

    private static Instruction parseInstruction(ByteBuffer in, int startPos, int bytesToParse, FunctionType[] types, AtomicInteger labelMarker) {
        if (in.position() - startPos >= bytesToParse) {
            throw new RuntimeException("Already reached end of code section");
        }
        int b = Byte.toUnsignedInt(in.get()); // get bytecode of instruction
        // System.out.printf("Parsing bytecode: 0x%x %d out of %d%n", b, in.position() - startPos, bytesToParse);
        return switch (b) {
            case NULL_UNREACHABLE, NULL_NOP, NULL_MEM_SIZE -> parseNullaryInstruction(b);
            case CF_BLOCK, CF_LOOP, CF_IF, CF_ELSE, CF_END, CF_BR, CF_BR_IF, CF_BR_TABLE -> parseControlFlowInstruction(b, in, types, startPos, bytesToParse, labelMarker);
            case CONST_INT, CONST_LONG, CONST_FLOAT, CONST_DOUBLE -> parseConstantInstruction(b, in);
            case GLOBAL_GET, GLOBAL_SET -> parseGlobalInstruction(b, in);
            case LOCAL_GET, LOCAL_SET, LOCAL_TEE, FUNC_CALL, FUNC_CALL_INDIRECT, FUNC_RETURN -> parseFunctionInstruction(b, in);
            case LOAD_I32, LOAD8_I32_S, LOAD8_I32_U, LOAD16_I32_S, LOAD16_I32_U, LOAD_F32, LOAD_F64,
                    LOAD_I64, LOAD8_I64_S, LOAD8_I64_U, LOAD16_I64_S,
                    LOAD16_I64_U, LOAD32_I64_S, LOAD32_I64_U -> parseLoadInstruction(b, in);
            case STORE_I32, STORE_I64, STORE_F32, STORE_F64, STORE8_I32, STORE16_I32,
                    STORE8_I64, STORE16_I64, STORE32_I64 -> parseStoreInstruction(b, in);
            case SELECT -> new Select();
            case UN_DROP, UN_MEM_GROW, UN_I32_EQZ, UN_I64_EQZ, UN_I32_CLZ, UN_I64_CLZ,
                    UN_I32_CTZ, UN_I64_CTZ, UN_I32_POPCNT, UN_I64_POPCNT,
                    UN_F32_ABS, UN_F32_NEG, UN_F32_CEIL, UN_F32_FLOOR, UN_F32_TRUNC, UN_F32_NEAREST, UN_F32_SQRT,
                    UN_F64_ABS, UN_F64_NEG, UN_F64_CEIL, UN_F64_FLOOR, UN_F64_TRUNC, UN_F64_NEAREST, UN_F64_SQRT,
                    UN_I32_WRAP_I64, UN_I32_TRUNC_F32_S, UN_I32_TRUNC_F32_U, UN_I32_TRUNC_F64_S, UN_I32_TRUNC_F64_U,
                    UN_I64_EXTEND_I32_S, UN_I64_EXTEND_I32_U, UN_I64_TRUNC_F32_S, UN_I64_TRUNC_F32_U, UN_I64_TRUNC_F64_S, UN_I64_TRUNC_F64_U,
                    UN_F32_CONVERT_I32_S, UN_F32_CONVERT_I32_U, UN_F32_CONVERT_I64_S, UN_F32_CONVERT_I64_U, UN_F32_DEMOTE_F64,
                    UN_F64_CONVERT_I32_S, UN_F64_CONVERT_I32_U, UN_F64_CONVERT_I64_S, UN_F64_CONVERT_I64_U, UN_F64_PROMOTE_F32,
                    UN_I32_REINTERPRET_F32, UN_I64_REINTERPRET_F64, UN_F32_REINTERPRET_I32, UN_F64_REINTERPRET_I64,
                    UN_I32_EXTEND8_S, UN_I32_EXTEND16_S, UN_I64_EXTEND8_S, UN_I64_EXTEND16_S, UN_I64_EXTEND32_S
                    -> parseUnaryInstruction(b, in);
            case BI_I32_EQ, BI_I32_NE, BI_I32_LT_S, BI_I32_LT_U, BI_I32_GT_S, BI_I32_GT_U,
                    BI_I32_LE_S, BI_I32_LE_U, BI_I32_GE_S, BI_I32_GE_U,
                    BI_I32_ADD, BI_I32_SUB, BI_I32_MUL, BI_I32_DIV_S, BI_I32_DIV_U, BI_I32_REM_S, BI_I32_REM_U,
                    BI_I32_AND, BI_I32_OR, BI_I32_XOR, BI_I32_SHL, BI_I32_SHR_S, BI_I32_SHR_U, BI_I32_ROTL, BI_I32_ROTR
                    -> parseI32BinaryInstruction(b, in);
            case BI_I64_EQ, BI_I64_NE, BI_I64_LT_S, BI_I64_LT_U, BI_I64_GT_S, BI_I64_GT_U,
                    BI_I64_LE_S, BI_I64_LE_U, BI_I64_GE_S, BI_I64_GE_U,
                    BI_I64_ADD, BI_I64_SUB, BI_I64_MUL, BI_I64_DIV_S, BI_I64_DIV_U, BI_I64_REM_S, BI_I64_REM_U,
                    BI_I64_AND, BI_I64_OR, BI_I64_XOR, BI_I64_SHL, BI_I64_SHR_S, BI_I64_SHR_U, BI_I64_ROTL, BI_I64_ROTR
                    -> parseI64BinaryInstruction(b, in);
            case BI_F32_EQ, BI_F32_NE, BI_F32_LT, BI_F32_GT, BI_F32_LE, BI_F32_GE,
                    BI_F32_ADD, BI_F32_SUB, BI_F32_MUL, BI_F32_DIV, BI_F32_MIN, BI_F32_MAX, BI_F32_COPYSIGN
                    -> parseF32BinaryInstruction(b, in);
            case BI_F64_EQ, BI_F64_NE, BI_F64_LT, BI_F64_GT, BI_F64_LE, BI_F64_GE,
                    BI_F64_ADD, BI_F64_SUB, BI_F64_MUL, BI_F64_DIV, BI_F64_MIN, BI_F64_MAX, BI_F64_COPYSIGN
                    -> parseF64BinaryInstruction(b, in);
            // other table instructions will be called using bytecode read by parseFCPrefixInstruction
            case TABLE_GET, TABLE_SET, REF_NULL, REF_IS_NULL, REF_FUNC -> parseRefTypeInstruction(b, in);
            case FC_PREFIX -> parseFCPrefixInstruction(in);
            default -> throw new RuntimeException(String.format("Invalid bytecode for instruction: 0x%x", b));
        };
    }

    private static ControlFlowInstruction parseControlFlowInstruction(int byteCode, ByteBuffer in, FunctionType[] types, int startPosition, int bytesToParse, AtomicInteger labelMarker) {
        /*
            https://www.w3.org/TR/wasm-core-2/syntax/instructions.html#control-instructions

            The `block`, `loop` and `if` instructions are structured instructions.
            They bracket nested sequences of instructions, called blocks, terminated with, or separated by, or pseudo-instructions.
            As the grammar prescribes, they must be well-nested.

            A structured instruction can consume input and produce output on the operand stack according to its annotated block type.
            It is given either as a type index that refers to a suitable function type,
            or as an optional value type inline, which is a shorthand for the function type [] -> [valtype].

            Each structured control instruction introduces an implicit label.
            Labels are targets for branch instructions that reference them with label indices.
            Unlike with other index spaces, indexing of labels is relative by nesting depth, that is,
            label refers to the innermost structured control instruction enclosing the referring branch instruction,
            while increasing indices refer to those farther out
         */
        int currentLevel = labelMarker.get();
        return switch (byteCode) {
            case CF_BR -> new ControlFlowInstruction.Branch(currentLevel - (int) Leb128.readUnsigned(in));
            case CF_BR_IF -> new ControlFlowInstruction.BranchIf(currentLevel - (int) Leb128.readUnsigned(in));
            case CF_END -> new ControlFlowInstruction.End();
            case CF_ELSE -> new ControlFlowInstruction.Else();
            case CF_BR_TABLE -> {
                int lsize = (int) Leb128.readUnsigned(in);
                int[] labels = new int[lsize];
                for (int i = 0; i < lsize; i++) {
                    labels[i] = currentLevel - (int) Leb128.readUnsigned(in);
                }
                int defaultLabel = currentLevel - (int) Leb128.readUnsigned(in);
                yield new ControlFlowInstruction.BranchTable(labels, defaultLabel);
            }
                 /*
                    TODO
                    Keep track of loop/block/if's . If you find next END, return block
                 */
            case CF_IF, CF_BLOCK, CF_LOOP -> {
                boolean isIf = byteCode == CF_IF;
                int blockType = (int) Leb128.readSigned(in);
                FunctionType ft = (blockType >= 0) ? types[blockType] : FunctionType.getBlockType(blockType);
                System.out.println("Func: " + ft);
                int label = labelMarker.incrementAndGet(); // -1 -> 0
                ArrayList<Instruction> insList = new ArrayList<>();
                while (in.position() - startPosition < bytesToParse) {
                    Instruction i = parseInstruction(in, startPosition, bytesToParse, types, labelMarker);
                    if (i instanceof ControlFlowInstruction.End || i instanceof ControlFlowInstruction.Else) {
                        if (i instanceof ControlFlowInstruction.Else) {
                            if (!isIf) {
                                throw new RuntimeException("Else block found for block/loop");
                            }
                            Instruction[] ifCode = insList.toArray(Instruction[]::new);
                            insList = new ArrayList<>();
                            // parse to end and return IfElse
                            while (in.position() - startPosition < bytesToParse) {
                                Instruction ii = parseInstruction(in, startPosition, bytesToParse, types, labelMarker);
                                if (ii instanceof ControlFlowInstruction.End) {
                                    labelMarker.decrementAndGet();
                                    yield new ControlFlowInstruction.IfElse(label, ft, ifCode, insList.toArray(Instruction[]::new));
                                }
                            }
                        }
                        break;
                    }
                    insList.add(i);
                }
                labelMarker.decrementAndGet();
                //throw new RuntimeException("Unexpected bytecode for control flow instruction: " + byteCode);
                if (byteCode == CF_IF) {
                    new ControlFlowInstruction.If(label, ft, insList.toArray(Instruction[]::new));
                }
                yield (byteCode == CF_BLOCK) ?
                        new ControlFlowInstruction.Block(label, ft, insList.toArray(Instruction[]::new)) :
                        new ControlFlowInstruction.Loop(label, ft, insList.toArray(Instruction[]::new));
            }
            default -> throw new RuntimeException("Unexpected bytecode for control flow instruction: " + byteCode);
        };
    }

    private static NullaryInstruction parseNullaryInstruction(int byteCode) {
        return switch (byteCode) {
            case NULL_UNREACHABLE -> NullaryInstruction.UNREACHABLE;
            case NULL_NOP -> NullaryInstruction.NOP;
            case NULL_MEM_SIZE -> NullaryInstruction.MEMORY_SIZE;
            default -> throw new RuntimeException("Unexpected bytecode for nullary instruction: " + byteCode);
        };
    }

    private static StoreInstruction parseStoreInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing Store instruction with bytecode 0x%X\n", byteCode);
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
        // System.out.printf("Parsing Load instruction with bytecode 0x%X\n", byteCode);
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
        // System.out.printf("Parsing Constant instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case CONST_INT -> new ConstInstruction.IntConst((int) Leb128.readSigned(in));
            case CONST_LONG -> new ConstInstruction.LongConst(Leb128.readSigned(in));
            case CONST_FLOAT -> new ConstInstruction.FloatConst(in.getFloat());
            case CONST_DOUBLE -> new ConstInstruction.DoubleConst(in.getDouble());
            default -> throw new RuntimeException("Unexpected bytecode for constant instruction: " + byteCode);
        };
    }

    private static FunctionInstruction parseFunctionInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing Function instruction with bytecode 0x%X\n", byteCode);
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
        // System.out.printf("Parsing global instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case GLOBAL_GET -> new GlobalInstruction.GlobalGet((int) Leb128.readUnsigned(in));
            case GLOBAL_SET -> new GlobalInstruction.GlobalSet((int) Leb128.readUnsigned(in));
            default -> throw new RuntimeException("Unexpected bytecode for function instruction: " + byteCode);
        };
    }

    private static UnaryInstruction parseUnaryInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing unary instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case UN_DROP -> UnaryInstruction.DROP;
            case UN_MEM_GROW -> UnaryInstruction.MEMORY_GROW;
            case UN_I32_EQZ -> UnaryInstruction.I32_EQZ;
            case UN_I32_CLZ -> UnaryInstruction.I32_CLZ;
            case UN_I32_CTZ -> UnaryInstruction.I32_CTZ;
            case UN_I32_POPCNT -> UnaryInstruction.I32_POPCNT;
            case UN_I64_EQZ -> UnaryInstruction.I64_EQZ;
            case UN_I64_CLZ -> UnaryInstruction.I64_CLZ;
            case UN_I64_CTZ -> UnaryInstruction.I64_CTZ;
            case UN_I64_POPCNT -> UnaryInstruction.I64_POPCNT;
            case UN_F32_ABS -> UnaryInstruction.F32_ABS;
            case UN_F32_NEG -> UnaryInstruction.F32_NEG;
            case UN_F32_CEIL -> UnaryInstruction.F32_CEIL;
            case UN_F32_FLOOR -> UnaryInstruction.F32_FLOOR;
            case UN_F32_TRUNC -> UnaryInstruction.F32_TRUNC;
            case UN_F32_NEAREST -> UnaryInstruction.F32_NEAREST;
            case UN_F32_SQRT -> UnaryInstruction.F32_SQRT;
            case UN_F64_ABS -> UnaryInstruction.F64_ABS;
            case UN_F64_NEG -> UnaryInstruction.F64_NEG;
            case UN_F64_CEIL -> UnaryInstruction.F64_CEIL;
            case UN_F64_FLOOR -> UnaryInstruction.F64_FLOOR;
            case UN_F64_TRUNC -> UnaryInstruction.F64_TRUNC;
            case UN_F64_NEAREST -> UnaryInstruction.F64_NEAREST;
            case UN_F64_SQRT -> UnaryInstruction.F64_SQRT;
            case UN_I32_WRAP_I64 -> UnaryInstruction.I32_WRAP_I64;
            case UN_I32_TRUNC_F32_S -> UnaryInstruction.I32_TRUNC_F32_S;
            case UN_I32_TRUNC_F32_U -> UnaryInstruction.I32_TRUNC_F32_U;
            case UN_I32_TRUNC_F64_S -> UnaryInstruction.I32_TRUNC_F64_S;
            case UN_I32_TRUNC_F64_U -> UnaryInstruction.I32_TRUNC_F64_U;
            case UN_I64_EXTEND_I32_S -> UnaryInstruction.I64_EXTEND_I32_S;
            case UN_I64_EXTEND_I32_U -> UnaryInstruction.I64_EXTEND_I32_U;
            case UN_I64_TRUNC_F32_S -> UnaryInstruction.I64_TRUNC_F32_S;
            case UN_I64_TRUNC_F32_U -> UnaryInstruction.I64_TRUNC_F32_U;
            case UN_I64_TRUNC_F64_S -> UnaryInstruction.I64_TRUNC_F64_S;
            case UN_I64_TRUNC_F64_U -> UnaryInstruction.I64_TRUNC_F64_U;
            case UN_F32_CONVERT_I32_S -> UnaryInstruction.F32_CONVERT_I32_S;
            case UN_F32_CONVERT_I32_U -> UnaryInstruction.F32_CONVERT_I32_U;
            case UN_F32_CONVERT_I64_S -> UnaryInstruction.F32_CONVERT_I64_S;
            case UN_F32_CONVERT_I64_U -> UnaryInstruction.F32_CONVERT_I64_U;
            case UN_F32_DEMOTE_F64 -> UnaryInstruction.F32_DEMOTE_F64;
            case UN_F64_CONVERT_I32_S -> UnaryInstruction.F64_CONVERT_I32_S;
            case UN_F64_CONVERT_I32_U -> UnaryInstruction.F64_CONVERT_I32_U;
            case UN_F64_CONVERT_I64_S -> UnaryInstruction.F64_CONVERT_I64_S;
            case UN_F64_CONVERT_I64_U -> UnaryInstruction.F64_CONVERT_I64_U;
            case UN_F64_PROMOTE_F32 -> UnaryInstruction.F64_PROMOTE_F32;
            case UN_I32_REINTERPRET_F32 -> UnaryInstruction.I32_REINTERPRET_F32;
            case UN_I64_REINTERPRET_F64 -> UnaryInstruction.I64_REINTERPRET_F64;
            case UN_F32_REINTERPRET_I32 -> UnaryInstruction.F32_REINTERPRET_I32;
            case UN_F64_REINTERPRET_I64 -> UnaryInstruction.F64_REINTERPRET_I64;
            case UN_I32_EXTEND8_S -> UnaryInstruction.I32_EXTEND8_S;
            case UN_I32_EXTEND16_S -> UnaryInstruction.I32_EXTEND16_S;
            case UN_I64_EXTEND8_S -> UnaryInstruction.I64_EXTEND8_S;
            case UN_I64_EXTEND16_S -> UnaryInstruction.I64_EXTEND16_S;
            case UN_I64_EXTEND32_S -> UnaryInstruction.I64_EXTEND32_S;
            default -> throw new RuntimeException("Unexpected bytecode for unary instruction: " + byteCode);
        };
    }

    private static IntBinaryInstruction parseI32BinaryInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing i32 binary instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case BI_I32_EQ -> IntBinaryInstruction.I32_EQ;
            case BI_I32_NE -> IntBinaryInstruction.I32_NE;
            case BI_I32_LT_S -> IntBinaryInstruction.I32_LT_S;
            case BI_I32_LT_U -> IntBinaryInstruction.I32_LT_U;
            case BI_I32_GT_S -> IntBinaryInstruction.I32_GT_S;
            case BI_I32_GT_U -> IntBinaryInstruction.I32_GT_U;
            case BI_I32_LE_S -> IntBinaryInstruction.I32_LE_S;
            case BI_I32_LE_U -> IntBinaryInstruction.I32_LE_U;
            case BI_I32_GE_S -> IntBinaryInstruction.I32_GE_S;
            case BI_I32_GE_U -> IntBinaryInstruction.I32_GE_U;
            case BI_I32_ADD -> IntBinaryInstruction.I32_ADD;
            case BI_I32_SUB -> IntBinaryInstruction.I32_SUB;
            case BI_I32_MUL -> IntBinaryInstruction.I32_MUL;
            case BI_I32_DIV_S -> IntBinaryInstruction.I32_DIV_S;
            case BI_I32_DIV_U -> IntBinaryInstruction.I32_DIV_U;
            case BI_I32_REM_S -> IntBinaryInstruction.I32_REM_S;
            case BI_I32_REM_U -> IntBinaryInstruction.I32_REM_U;
            case BI_I32_AND -> IntBinaryInstruction.I32_AND;
            case BI_I32_OR -> IntBinaryInstruction.I32_OR;
            case BI_I32_XOR -> IntBinaryInstruction.I32_XOR;
            case BI_I32_SHL -> IntBinaryInstruction.I32_SHL;
            case BI_I32_SHR_S -> IntBinaryInstruction.I32_SHR_S;
            case BI_I32_SHR_U -> IntBinaryInstruction.I32_SHR_U;
            case BI_I32_ROTL -> IntBinaryInstruction.I32_ROTL;
            case BI_I32_ROTR -> IntBinaryInstruction.I32_ROTR;
            default -> throw new RuntimeException("Unexpected bytecode for i32 binary instruction: " + byteCode);
        };
    }

    private static LongBinaryInstruction parseI64BinaryInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing i64 binary instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case BI_I64_EQ -> LongBinaryInstruction.I64_EQ;
            case BI_I64_NE -> LongBinaryInstruction.I64_NE;
            case BI_I64_LT_S -> LongBinaryInstruction.I64_LT_S;
            case BI_I64_LT_U -> LongBinaryInstruction.I64_LT_U;
            case BI_I64_GT_S -> LongBinaryInstruction.I64_GT_S;
            case BI_I64_GT_U -> LongBinaryInstruction.I64_GT_U;
            case BI_I64_LE_S -> LongBinaryInstruction.I64_LE_S;
            case BI_I64_LE_U -> LongBinaryInstruction.I64_LE_U;
            case BI_I64_GE_S -> LongBinaryInstruction.I64_GE_S;
            case BI_I64_GE_U -> LongBinaryInstruction.I64_GE_U;
            case BI_I64_ADD -> LongBinaryInstruction.I64_ADD;
            case BI_I64_SUB -> LongBinaryInstruction.I64_SUB;
            case BI_I64_MUL -> LongBinaryInstruction.I64_MUL;
            case BI_I64_DIV_S -> LongBinaryInstruction.I64_DIV_S;
            case BI_I64_DIV_U -> LongBinaryInstruction.I64_DIV_U;
            case BI_I64_REM_S -> LongBinaryInstruction.I64_REM_S;
            case BI_I64_REM_U -> LongBinaryInstruction.I64_REM_U;
            case BI_I64_AND -> LongBinaryInstruction.I64_AND;
            case BI_I64_OR -> LongBinaryInstruction.I64_OR;
            case BI_I64_XOR -> LongBinaryInstruction.I64_XOR;
            case BI_I64_SHL -> LongBinaryInstruction.I64_SHL;
            case BI_I64_SHR_S -> LongBinaryInstruction.I64_SHR_S;
            case BI_I64_SHR_U -> LongBinaryInstruction.I64_SHR_U;
            case BI_I64_ROTL -> LongBinaryInstruction.I64_ROTL;
            case BI_I64_ROTR -> LongBinaryInstruction.I64_ROTR;
            default -> throw new RuntimeException("Unexpected bytecode for i64 binary instruction: " + byteCode);
        };
    }

    private static FloatBinaryInstruction parseF32BinaryInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing f32 binary instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case BI_F32_EQ -> FloatBinaryInstruction.F32_EQ;
            case BI_F32_NE -> FloatBinaryInstruction.F32_NE;
            case BI_F32_LT -> FloatBinaryInstruction.F32_LT;
            case BI_F32_GT -> FloatBinaryInstruction.F32_GT;
            case BI_F32_LE -> FloatBinaryInstruction.F32_LE;
            case BI_F32_GE -> FloatBinaryInstruction.F32_GE;
            case BI_F32_ADD -> FloatBinaryInstruction.F32_ADD;
            case BI_F32_SUB -> FloatBinaryInstruction.F32_SUB;
            case BI_F32_MUL -> FloatBinaryInstruction.F32_MUL;
            case BI_F32_DIV -> FloatBinaryInstruction.F32_DIV;
            case BI_F32_MIN -> FloatBinaryInstruction.F32_MIN;
            case BI_F32_MAX -> FloatBinaryInstruction.F32_MAX;
            case BI_F32_COPYSIGN -> FloatBinaryInstruction.F32_COPY_SIGN;
            default -> throw new RuntimeException("Unexpected bytecode for f32 binary instruction: " + byteCode);
        };
    }

    private static DoubleBinaryInstruction parseF64BinaryInstruction(int byteCode, ByteBuffer in) {
        // System.out.printf("Parsing F64 binary instruction with bytecode 0x%X\n", byteCode);
        return switch (byteCode) {
            case BI_F64_EQ -> DoubleBinaryInstruction.F64_EQ;
            case BI_F64_NE -> DoubleBinaryInstruction.F64_NE;
            case BI_F64_LT -> DoubleBinaryInstruction.F64_LT;
            case BI_F64_GT -> DoubleBinaryInstruction.F64_GT;
            case BI_F64_LE -> DoubleBinaryInstruction.F64_LE;
            case BI_F64_GE -> DoubleBinaryInstruction.F64_GE;
            case BI_F64_ADD -> DoubleBinaryInstruction.F64_ADD;
            case BI_F64_SUB -> DoubleBinaryInstruction.F64_SUB;
            case BI_F64_MUL -> DoubleBinaryInstruction.F64_MUL;
            case BI_F64_DIV -> DoubleBinaryInstruction.F64_DIV;
            case BI_F64_MIN -> DoubleBinaryInstruction.F64_MIN;
            case BI_F64_MAX -> DoubleBinaryInstruction.F64_MAX;
            case BI_F64_COPYSIGN -> DoubleBinaryInstruction.F64_COPY_SIGN;
            default -> throw new RuntimeException("Unexpected bytecode for F64 binary instruction: " + byteCode);
        };
    }

    private static Instruction parseFCPrefixInstruction(ByteBuffer in) {
        int byteCode = Byte.toUnsignedInt(in.get());
        return switch (byteCode) {
            case FC_MEM_INIT -> new SegmentInstruction.MemoryInit((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case FC_DATA_DROP -> new SegmentInstruction.DataDrop((int) Leb128.readUnsigned(in));
            case FC_MEM_COPY -> new SegmentInstruction.MemoryCopy((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case FC_MEM_FILL -> new SegmentInstruction.MemoryFill((int) Leb128.readUnsigned(in));
            case FC_TABLE_INIT, FC_ELEM_DROP, FC_TABLE_COPY, FC_TABLE_GROW, FC_TABLE_SIZE, FC_TABLE_FILL -> parseRefTypeInstruction(byteCode, in);
            default -> throw new RuntimeException("Unexpected bytecode for FC Prefix instruction: " + byteCode);
        };
    }

    private static RefTypeInstruction parseRefTypeInstruction(int byteCode, ByteBuffer in) {
        return switch (byteCode) {
            case REF_NULL -> new RefTypeInstruction.RefNull(ValueType.RefType.from(in.get()));
            case REF_IS_NULL -> new RefTypeInstruction.RefIsNull();
            case REF_FUNC -> new RefTypeInstruction.RefFunc((int) Leb128.readUnsigned(in));
            case TABLE_GET -> new RefTypeInstruction.TableGet((int) Leb128.readUnsigned(in));
            case TABLE_SET -> new RefTypeInstruction.TableSet((int) Leb128.readUnsigned(in));
            // bytes from FC_PREFIX
            case FC_TABLE_INIT -> new RefTypeInstruction.TableInit((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case FC_TABLE_COPY -> new RefTypeInstruction.TableCopy((int) Leb128.readUnsigned(in), (int) Leb128.readUnsigned(in));
            case FC_TABLE_GROW -> new RefTypeInstruction.TableGrow((int) Leb128.readUnsigned(in));
            case FC_TABLE_SIZE -> new RefTypeInstruction.TableSize((int) Leb128.readUnsigned(in));
            case FC_TABLE_FILL -> new RefTypeInstruction.TableFill((int) Leb128.readUnsigned(in));
            case FC_ELEM_DROP -> new RefTypeInstruction.ElemDrop((int) Leb128.readUnsigned(in));
            default -> throw new RuntimeException("Unexpected bytecode for FC Prefix instruction: " + byteCode);
        };
    }
}
