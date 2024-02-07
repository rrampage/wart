package rrampage.wasp.data;

import rrampage.wasp.instructions.ConstExpression;
import rrampage.wasp.parser.types.ImportDescriptor;

import static rrampage.wasp.utils.ConversionUtils.*;

public sealed interface Variable {
    int REF_NULL = -1;
    Function NULL_FUNC = new Function("NULL_FUNC_WASP", FunctionType.VOID, null, null);
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

    default long getValAsLong() {
        return switch (this) {
            case I32Variable v -> v.getVal();
            case F32Variable v -> floatToLong(v.getVal());
            case F64Variable v -> doubleToLong(v.getVal());
            case I64Variable v -> v.getVal();
            case FuncrefVariable v -> v.getVal();
        };
    }
    boolean isMutable();
    ConstExpression expr();

    default ValueType type() {
        return switch (this) {
            case F32Variable v -> F32Variable.type ;
            case F64Variable v -> F64Variable.type;
            case I32Variable v -> I32Variable.type;
            case I64Variable v -> I64Variable.type;
            case FuncrefVariable v -> FuncrefVariable.type;
        };
    }

    final class I32Variable implements Variable {
        public static final ValueType type = ValueType.NumType.I32;
        private int val;
        private final boolean isMutable;
        private ConstExpression expr;
        I32Variable(int val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        I32Variable(ConstExpression expr, boolean isMutable) {
            this.expr = expr;
            this.isMutable = isMutable;
        }
        public int getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
        public ConstExpression expr() {return expr;}
    }

    final class I64Variable implements Variable {
        public static final ValueType type = ValueType.NumType.I64;
        private long val;
        private final boolean isMutable;
        private ConstExpression expr;
        I64Variable(long val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        I64Variable(ConstExpression expr, boolean isMutable) {
            this.expr = expr;
            this.isMutable = isMutable;
        }
        public long getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
        public ConstExpression expr() {return expr;}
    }

    final class F32Variable implements Variable {
        public static final ValueType type = ValueType.NumType.F32;
        private float val;
        private final boolean isMutable;
        private ConstExpression expr;
        F32Variable(float val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        F32Variable(ConstExpression expr, boolean isMutable) {
            this.expr = expr;
            this.isMutable = isMutable;
        }
        public float getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
        public ConstExpression expr() {return expr;}
    }

    final class F64Variable implements Variable {
        public static final ValueType type = ValueType.NumType.F64;
        private double val;
        private final boolean isMutable;
        private ConstExpression expr;
        F64Variable(double val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        F64Variable(ConstExpression expr, boolean isMutable) {
            this.expr = expr;
            this.isMutable = isMutable;
        }
        public double getVal() { return val;}
        public boolean isMutable() { return this.isMutable; }
        public ConstExpression expr() {return expr;}
    }

    final class FuncrefVariable implements Variable {
        public static final ValueType type = ValueType.RefType.FUNCREF;
        private long val;
        private final boolean isMutable;
        private ConstExpression expr;
        FuncrefVariable(long val, boolean isMutable) {
            this.val = val;
            this.isMutable = isMutable;
        }
        FuncrefVariable(ConstExpression expr, boolean isMutable) {
            this.expr = expr;
            this.isMutable = isMutable;
        }
        public long getVal(){ return val;}
        public boolean isMutable() { return this.isMutable; }
        public ConstExpression expr() {return expr;}
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

    static Variable newVariable(ValueType type, ConstExpression expr, boolean isMutable) {
        return switch (type) {
            case ValueType.NumType.I32 -> new I32Variable(expr, isMutable);
            case ValueType.NumType.I64 -> new I64Variable(expr, isMutable);
            case ValueType.NumType.F32 -> new F32Variable(expr, isMutable);
            case ValueType.NumType.F64 -> new F64Variable(expr, isMutable);
            case ValueType.RefType.FUNCREF -> new FuncrefVariable(expr, isMutable);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    static String debug(Variable variable) {
        return switch (variable) {
            case F32Variable v -> variable.type() + " " + v.getVal();
            case F64Variable v -> variable.type() + " " + v.getVal();
            case I32Variable v -> variable.type() + " " + v.getVal();
            case I64Variable v -> variable.type() + " " + v.getVal();
            case FuncrefVariable v -> variable.type() + " " + v.getVal();
        };
    }

    default boolean matchesDescriptor(ImportDescriptor.GlobalDescriptor d) {
        return d.valueType().equals(type()) && d.mutable() == isMutable();
    }
}