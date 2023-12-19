package rrampage.wasp.instructions;

public class ByteCodeConstants {
    // Constant instructions
    public static final int CONST_INT = 0x41, CONST_LONG = 0x42, CONST_FLOAT = 0x43, CONST_DOUBLE = 0x44;

    // Call, call_indirect and return
    public static final int FUNC_RETURN = 0xf, FUNC_CALL = 0x10, FUNC_CALL_INDIRECT = 0x11;


    // Local and global get and set
    public static final int LOCAL_GET = 0x20, LOCAL_SET = 0x21, LOCAL_TEE = 0x22;
    public static final int GLOBAL_GET = 0x23, GLOBAL_SET = 0x24;

    // Load
    public static final int LOAD_I32 = 0x28, LOAD_I64 = 0x29, LOAD_F32 = 0x2a, LOAD_F64 = 0x2b,
            LOAD8_I32_S = 0x2c, LOAD8_I32_U = 0x2d, LOAD16_I32_S = 0x2e, LOAD16_I32_U = 0x2f,
            LOAD8_I64_S = 0x30, LOAD8_I64_U = 0x31, LOAD16_I64_S = 0x32, LOAD16_I64_U = 0x33, LOAD32_I64_S = 0x34, LOAD32_I64_U = 0x35;

    // Store
    public static final int STORE_I32 = 0x36, STORE_I64 = 0x37, STORE_F32 = 0x38, STORE_F64 = 0x39,
            STORE8_I32 = 0x3a, STORE16_I32 = 0x3b, STORE8_I64 = 0x3c, STORE16_I64 = 0x3d, STORE32_I64 = 0x3e;
}
