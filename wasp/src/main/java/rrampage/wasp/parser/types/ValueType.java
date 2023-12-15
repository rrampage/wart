package rrampage.wasp.parser.types;

import rrampage.wasp.data.DataType;

import java.util.HashMap;

public enum ValueType {
    I32((byte) 0x7f),
    I64((byte) 0x7e),
    F32((byte) 0x7d),
    F64((byte) 0x7c),
    VEC((byte) 0x7b),
    FUNCREF((byte) 0x70),
    EXTERNREF((byte) 0x6f),
    ;

    private static final HashMap<Byte, ValueType> typeMap = generateTypeMap();

    private final byte val;
    ValueType(byte val) {
        this.val = val;
    }
    public static ValueType of(byte val) {
        return typeMap.get(val);
    }

    public static DataType fromValueType(ValueType vt) {
        return switch (vt) {
            case I32 -> DataType.I32;
            case I64 -> DataType.I64;
            case F32 -> DataType.F32;
            case F64 -> DataType.F64;
            default -> throw new RuntimeException("Unsupported datatype " + vt.name());
        };
    }

    private static HashMap<Byte, ValueType> generateTypeMap() {
        HashMap<Byte, ValueType> typeMap = new HashMap<>();
        for (ValueType bt : ValueType.values()) {
            typeMap.put(bt.val, bt);
        }
        return typeMap;
    }
}
