package rrampage.wasp.instructions;

public class ByteCodeConstants {
    public static final int NULL_UNREACHABLE = 0x0, NULL_NOP = 0x01, // nop, unreachable
    CF_BLOCK = 0x2, CF_LOOP = 0x3, CF_IF = 0x4, CF_ELSE = 0x5, CF_END = 0xb,
            CF_BR = 0xc, CF_BR_IF = 0xd, CF_BR_TABLE = 0xe, // Control flow
    FUNC_RETURN = 0xf, FUNC_CALL = 0x10, FUNC_CALL_INDIRECT = 0x11, // Call, call_indirect and return
    UN_DROP = 0x1a, SELECT = 0x1b, // drop and select
    LOCAL_GET = 0x20, LOCAL_SET = 0x21, LOCAL_TEE = 0x22, GLOBAL_GET = 0x23, GLOBAL_SET = 0x24,
    LOAD_I32 = 0x28, LOAD_I64 = 0x29, LOAD_F32 = 0x2a, LOAD_F64 = 0x2b,
        LOAD8_I32_S = 0x2c, LOAD8_I32_U = 0x2d, LOAD16_I32_S = 0x2e, LOAD16_I32_U = 0x2f,
        LOAD8_I64_S = 0x30, LOAD8_I64_U = 0x31, LOAD16_I64_S = 0x32, LOAD16_I64_U = 0x33, LOAD32_I64_S = 0x34, LOAD32_I64_U = 0x35,
    STORE_I32 = 0x36, STORE_I64 = 0x37, STORE_F32 = 0x38, STORE_F64 = 0x39,
        STORE8_I32 = 0x3a, STORE16_I32 = 0x3b, STORE8_I64 = 0x3c, STORE16_I64 = 0x3d, STORE32_I64 = 0x3e,
    NULL_MEM_SIZE = 0x3f, UN_MEM_GROW = 0x40, // memory
    CONST_INT = 0x41, CONST_LONG = 0x42, CONST_FLOAT = 0x43, CONST_DOUBLE = 0x44, // Constant instructions
    UN_I32_EQZ = 0x45, BI_I32_EQ = 0x46, BI_I32_NE = 0x47, BI_I32_LT_S = 0x48, BI_I32_LT_U = 0x49,  BI_I32_GT_S = 0x4a, BI_I32_GT_U = 0x4b,
        BI_I32_LE_S = 0x4c, BI_I32_LE_U = 0x4d,  BI_I32_GE_S = 0x4e, BI_I32_GE_U = 0x4f, // i32 comparison instructions
    UN_I64_EQZ = 0x50, BI_I64_EQ = 0x51, BI_I64_NE = 0x52, BI_I64_LT_S = 0x53, BI_I64_LT_U = 0x54,  BI_I64_GT_S = 0x55, BI_I64_GT_U = 0x56,
        BI_I64_LE_S = 0x57, BI_I64_LE_U = 0x58,  BI_I64_GE_S = 0x59, BI_I64_GE_U = 0x5a, // i64 comparison instructions
    BI_F32_EQ = 0x5b, BI_F32_NE = 0x5c, BI_F32_LT = 0x5d, BI_F32_GT = 0x5e, BI_F32_LE = 0x5f, BI_F32_GE = 0x60, // f32 comparison instructions
    BI_F64_EQ = 0x61, BI_F64_NE = 0x62, BI_F64_LT = 0x63, BI_F64_GT = 0x64, BI_F64_LE = 0x65, BI_F64_GE = 0x66, // f64 comparison instructions
    UN_I32_CLZ = 0x67, UN_I32_CTZ = 0x68, UN_I32_POPCNT = 0x69,
    BI_I32_ADD = 0x6a, BI_I32_SUB = 0x6b, BI_I32_MUL = 0x6c, BI_I32_DIV_S = 0x6d, BI_I32_DIV_U = 0x6e, BI_I32_REM_S = 0x6f, BI_I32_REM_U = 0x70, // i32 math
    BI_I32_AND = 0x71, BI_I32_OR = 0x72, BI_I32_XOR = 0x73, BI_I32_SHL = 0x74, BI_I32_SHR_S = 0x75, BI_I32_SHR_U = 0x76, BI_I32_ROTL = 0x77, BI_I32_ROTR = 0x78, // i32 bitwise
    UN_I64_CLZ = 0x79, UN_I64_CTZ = 0x7a, UN_I64_POPCNT = 0x7b,
    BI_I64_ADD = 0x7c, BI_I64_SUB = 0x7d, BI_I64_MUL = 0x7e, BI_I64_DIV_S = 0x7f, BI_I64_DIV_U = 0x80, BI_I64_REM_S = 0x81, BI_I64_REM_U = 0x82, // i64 math
    BI_I64_AND = 0x83, BI_I64_OR = 0x84, BI_I64_XOR = 0x85, BI_I64_SHL = 0x86, BI_I64_SHR_S = 0x87, BI_I64_SHR_U = 0x88, BI_I64_ROTL = 0x89, BI_I64_ROTR = 0x8a, // i64 bitwise
    DUMMY = -1
    ;
}
