package rrampage.wasp.data;

import rrampage.wasp.utils.ConversionUtils;

import java.util.HashMap;
import java.util.Map;

public sealed interface ValueType {
    enum VecType implements ValueType {
        VEC_TYPE((byte) 0x7b);
        private final byte val;
        VecType(byte val) {this.val = val;}
        public byte byteCode() {return val;}
        public String toString() {return this.name();}
    }

    enum NumType implements ValueType {
        I32((byte) 0x7f),
        I64((byte) 0x7e),
        F32((byte) 0x7d),
        F64((byte) 0x7c);
        private final byte val;
        NumType(byte val) {this.val = val;}
        public byte byteCode() {return val;}
        public String toString() {return this.name();}
        private static final Map<Byte, NumType> numTypeMap = ConversionUtils.convertArrayToImmutableMap(NumType.values(), NumType::byteCode);
        public static NumType from(byte b) {
            NumType nt = numTypeMap.get(b);
            if (nt == null) {
                throw new RuntimeException("Invalid numType for bytecode " + b);
            }
            return nt;
        }
    }

    enum RefType implements ValueType {
        FUNCREF((byte) 0x70),
        EXTERNREF((byte) 0x6f);
        private final byte val;
        RefType(byte val) {this.val = val;}
        public byte byteCode() {return val;}
        public String toString() {return this.name();}
        private static final Map<Byte, RefType> typeMap = ConversionUtils.convertArrayToImmutableMap(RefType.values(), RefType::byteCode);
        public static RefType from(byte b) {
            RefType t = typeMap.get(b);
            if (t == null) {
                throw new RuntimeException("Invalid refType for bytecode " + b);
            }
            return t;
        }
    }
    byte byteCode();

    static ValueType from(byte b) {
        return typeMap.get(b);
    }
    static Map<Byte, ValueType> typeMap = Map.copyOf(generateTypeMap());
    private static Map<Byte, ValueType> generateTypeMap() {
        HashMap<Byte, ValueType> typeMap = new HashMap<>(NumType.numTypeMap);
        typeMap.putAll(RefType.typeMap);
        for (var t : VecType.values()) { typeMap.put(t.byteCode(), t);}
        return typeMap;
    }
}
