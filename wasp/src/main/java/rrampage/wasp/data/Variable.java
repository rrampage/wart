package rrampage.wasp.data;

import static rrampage.wasp.utils.ConversionUtils.*;

public sealed interface Variable {
    void setVal(long val);
    final class I32Variable implements Variable {
        private int val;
        I32Variable(int val) {
            this.val = val;
        }
        public ValueType.NumType getType() {return ValueType.NumType.I32;}
        public int getVal() { return val;}
        public void setVal(long val) { this.val = longToInt(val);}
    }

    final class I64Variable implements Variable {
        private long val;
        I64Variable(long val) {
            this.val = val;
        }
        public long getVal() { return val;}
        public void setVal(long val) { this.val = val;}
        public ValueType.NumType getType() {return ValueType.NumType.I64;}
    }

    final class F32Variable implements Variable {
        private float val;
        F32Variable(float val) {
            this.val = val;
        }
        public float getVal() { return val;}
        public void setVal(long val) { this.val = longToFloat(val);}
        public ValueType.NumType getType() {return ValueType.NumType.F32;}
    }

    final class F64Variable implements Variable {
        private double val;
        F64Variable(double val) {
            this.val = val;
        }
        public double getVal() { return val;}
        public void setVal(long val) { this.val = longToDouble(val);}
        public ValueType.NumType getType() {return ValueType.NumType.F64;}
    }

    static Variable newVariable(ValueType.NumType numType, long val) {
        return switch (numType) {
            case I32 -> new I32Variable(longToInt(val));
            case I64 -> new I64Variable(val);
            case F32 -> new F32Variable(longToFloat(val));
            case F64 -> new F64Variable(longToDouble(val));
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
