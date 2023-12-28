package rrampage.wasp.data;

import static rrampage.wasp.utils.ConversionUtils.*;

public sealed interface Variable {
    default void setVal(long val) {
        if (!isMutable()) {
            throw new IllegalStateException("Can not set immutable variable");
        }
        switch (this) {
            case I32Variable v -> v.val = longToInt(val);
            case F32Variable v -> v.val = longToFloat(val);
            case F64Variable v -> v.val = longToDouble(val);
            case I64Variable v -> v.val = val;
            case FuncrefVariable v -> v.val = val;
        }
    }
    boolean isMutable();
    final class I32Variable implements Variable {
        public static final ValueType type = ValueType.NumType.I32;
        private int val;
        private final boolean isMutable;
        I32Variable(int val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public int getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
    }

    final class I64Variable implements Variable {
        public static final ValueType type = ValueType.NumType.I64;
        private long val;
        private final boolean isMutable;
        I64Variable(long val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public long getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
    }

    final class F32Variable implements Variable {
        public static final ValueType type = ValueType.NumType.F32;
        private float val;
        private final boolean isMutable;
        F32Variable(float val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public float getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
    }

    final class F64Variable implements Variable {
        public static final ValueType type = ValueType.NumType.F64;
        private double val;
        private final boolean isMutable;
        F64Variable(double val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public double getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
    }

    final class FuncrefVariable implements Variable {
        public static final ValueType type = ValueType.RefType.FUNCREF;
        private long val;
        private final boolean isMutable;
        FuncrefVariable(long val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public long getVal(){ return val;}
        public boolean isMutable() { return this.isMutable; }
    }

    static Variable newMutableVariable(ValueType type, long val) {
        return newVariable(type, val, true);
    }

    static Variable newVariable(ValueType type, long val, boolean isMutable) {
        return switch (type) {
            case ValueType.NumType.I32 -> new I32Variable(longToInt(val), isMutable);
            case ValueType.NumType.I64 -> new I64Variable(val, isMutable);
            case ValueType.NumType.F32 -> new F32Variable(longToFloat(val), isMutable);
            case ValueType.NumType.F64 -> new F64Variable(longToDouble(val), isMutable);
            case ValueType.RefType.FUNCREF -> new FuncrefVariable(val, isMutable);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    static String debug(Variable variable) {
        return switch (variable) {
            case F32Variable v -> F32Variable.type + " " + v.getVal();
            case F64Variable v -> F64Variable.type + " " + v.getVal();
            case I32Variable v -> I32Variable.type + " " + v.getVal();
            case I64Variable v -> I64Variable.type + " " + v.getVal();
            case FuncrefVariable v -> FuncrefVariable.type + " " + v.getVal();
        };
    }
}