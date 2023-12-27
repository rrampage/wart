package rrampage.wasp.data;

import static rrampage.wasp.utils.ConversionUtils.*;

public sealed interface Variable {
    default void setVal(long val) {
        if (!isMutable()) {
            throw new IllegalStateException("Can not set immutable variable");
        }
        switch (this) {
            case I32Variable v -> v.setVal(val);
            case F32Variable v -> v.setVal(val);
            case F64Variable v -> v.setVal(val);
            case I64Variable v -> v.setVal(val);
        }
    }
    boolean isMutable();
    final class I32Variable implements Variable {
        private int val;
        private final boolean isMutable;
        I32Variable(int val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public int getVal() { return val;}
        public void setVal(long val) {this.val = longToInt(val);}
        public boolean isMutable() { return this.isMutable; }
        public ValueType.NumType getType() {return ValueType.NumType.I32;}
    }

    final class I64Variable implements Variable {
        private long val;
        private final boolean isMutable;
        I64Variable(long val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public long getVal() { return val;}
        public void setVal(long val) { this.val = val;}
        public boolean isMutable() { return this.isMutable; }
        public ValueType.NumType getType() {return ValueType.NumType.I64;}
    }

    final class F32Variable implements Variable {
        private float val;
        private final boolean isMutable;
        F32Variable(float val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public float getVal() { return val;}
        public void setVal(long val) { this.val = longToFloat(val);}
        public boolean isMutable() { return this.isMutable; }
        public ValueType.NumType getType() {return ValueType.NumType.F32;}
    }

    final class F64Variable implements Variable {
        private double val;
        private final boolean isMutable;
        F64Variable(double val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        public double getVal() { return val;}
        public void setVal(long val) { this.val = longToDouble(val);}
        public boolean isMutable() { return this.isMutable; }
        public ValueType.NumType getType() {return ValueType.NumType.F64;}
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
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    static String debug(Variable variable) {
        return switch (variable) {
            case F32Variable v -> v.getType().toString() + " " + v.getVal();
            case F64Variable v -> v.getType().toString() + " " + v.getVal();
            case I32Variable v -> v.getType().toString() + " " + v.getVal();
            case I64Variable v -> v.getType().toString() + " " + v.getVal();
        };
    }
}