package rrampage.wasp.instructions;

public class ByteCodeConstants {
    // Constant instructions
    public static final int CONST_INT = 0x41;
    public static final int CONST_LONG = 0x42;
    public static final int CONST_FLOAT = 0x43;
    public static final int CONST_DOUBLE = 0x44;

    // Call, call_indirect and return
    public static final int FUNC_RETURN = 0xf;
    public static final int FUNC_CALL = 0x10;
    public static final int FUNC_CALL_INDIRECT = 0x11;


    // Local and global get and set
    public static final int LOCAL_GET = 0x20;
    public static final int LOCAL_SET = 0x21;
    public static final int LOCAL_TEE = 0x22;
    public static final int GLOBAL_GET = 0x23;
    public static final int GLOBAL_SET = 0x24;

}
