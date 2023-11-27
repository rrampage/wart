package rrampage.waja.wasm;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;

import static rrampage.waja.utils.ConversionUtils.*;


public class Machine {
    private final ArrayDeque<Long> stack; // Store everything as long. Convert to type as per instruction
    private byte[] memory;
    private final Function[] functions;

    public Machine(Function[] functions, int memSize) {
       this.stack = new ArrayDeque<>(8192);
       this.memory = new byte[memSize];
       this.functions = functions;
    }

    public long pop() {
        return stack.pop();
    }

    public int popInt() {
        return longToInt(stack.pop());
    }

    public float popFloat() {
        return longToFloat(stack.pop());
    }

    public double popDouble() {
        return longToDouble(stack.pop());
    }

    public void push(long val) {
        stack.push(val);
    }

    public void pushInt(int val) {
        stack.push(intToLong(val));
    }

    public void pushFloat(float val) {
        stack.push(floatToLong(val));
    }

    public void pushDouble(double val) {
        stack.push(doubleToLong(val));
    }

    public void store(int addr, byte[] data) {
        if (addr >= memory.length) {
            throw new RuntimeException("Invalid address passed to memory: " + addr);
        }
        System.arraycopy(data, 0, memory, addr, data.length);
    }

    public byte[] load(int addr, int offset) {
        return Arrays.copyOfRange(memory, addr, addr + offset);
    }

    public Variable[] call(Function fun) {
        // Creating a "scratch space" of variables for function params as well as local vars to be used in function body
        Variable[] locals = new Variable[fun.numParams() + fun.numLocals()];
        // LIFO for function params as params are pushed to stack and must be popped in reverse order
        for (int i = fun.numParams()-1; i >= 0; i-- ) {
            locals[i] = Variable.newVariable(fun.paramTypes()[i], pop());
        }
        for (int i = fun.numParams(); i < locals.length; i++) {
            locals[i] = Variable.newVariable(fun.locals()[i - fun.numParams()], 0);
        }
        execute(fun.code(), locals);
        if (fun.isVoidReturn()) {
            return null;
        }
        int n = fun.returnTypes().length;
        Variable[] returns = new Variable[n];
        for (int i = 0; i < n; i++) {
            returns[i] = Variable.newVariable(fun.returnTypes()[i], pop());
        }
        return returns;
    }
    public void execute(Instruction[] instructions, Variable[] locals) {
        for (Instruction ins : instructions) {
            System.out.println("Instruction: " + ins.opCode());
            switch (ins) {
                case DoubleConst c -> pushDouble(c.val());
                case FloatConst c -> pushFloat(c.val());
                case IntConst c -> pushInt(c.val());
                case LongConst c -> push(c.val());
                case DoubleBinaryInstruction b -> {
                    double l = popDouble();
                    double r = popDouble();
                    switch (b) {
                        case F64_ADD -> pushDouble(l+r);
                        case F64_SUB -> pushDouble(l-r);
                        case F64_MUL -> pushDouble(l*r);
                        case F64_DIV -> pushDouble(l/r);
                        case F64_MAX -> pushDouble(Double.max(l,r));
                        case F64_MIN -> pushDouble(Double.min(l,r));
                        case F64_COPY_SIGN -> pushDouble((l*r >= 0.0) ? l : -l);
                        case F64_EQ -> pushInt(wrapBoolean(l == r));
                        case F64_NE -> pushInt(wrapBoolean(l != r));
                        case F64_GE -> pushInt(wrapBoolean(l >= r));
                        case F64_GT -> pushInt(wrapBoolean(l > r));
                        case F64_LE -> pushInt(wrapBoolean(l <= r));
                        case F64_LT -> pushInt(wrapBoolean(l < r));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case FloatBinaryInstruction b -> {
                    float l = popFloat();
                    float r = popFloat();
                    switch (b) {
                        case F32_ADD -> pushFloat(l+r);
                        case F32_SUB -> pushFloat(l-r);
                        case F32_MUL -> pushFloat(l*r);
                        case F32_DIV -> pushFloat(l/r);
                        case F32_MAX -> pushFloat(Float.max(l,r));
                        case F32_MIN -> pushFloat(Float.min(l,r));
                        case F32_COPY_SIGN -> pushFloat((l*r >= 0.0f) ? l : -l);
                        case F32_EQ -> pushInt(wrapBoolean(l == r));
                        case F32_NE -> pushInt(wrapBoolean(l != r));
                        case F32_GE -> pushInt(wrapBoolean(l >= r));
                        case F32_GT -> pushInt(wrapBoolean(l > r));
                        case F32_LE -> pushInt(wrapBoolean(l <= r));
                        case F32_LT -> pushInt(wrapBoolean(l < r));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case LongBinaryInstruction b -> {
                    long l = pop();
                    long r = pop();
                    switch (b) {
                        case I64_ADD -> push(l+r);
                        case I64_SUB -> push(l-r);
                        case I64_MUL -> push(l*r);
                        case I64_DIV_S -> push(l/r);
                        case I64_REM_S -> push(l % r);
                        case I64_DIV_U -> push(Long.divideUnsigned(l,r));
                        case I64_REM_U -> push(Long.remainderUnsigned(l,r));
                        case I64_MAX -> push(Long.max(l,r));
                        case I64_MIN -> push(Long.min(l,r));
                        case I64_AND -> push(l&r);
                        case I64_OR -> push(l|r);
                        case I64_XOR -> push(l^r);
                        case I64_EQ -> pushInt(wrapBoolean(l == r));
                        case I64_NE -> pushInt(wrapBoolean(l != r));
                        case I64_GE_S -> pushInt(wrapBoolean(l >= r));
                        case I64_GT_S -> pushInt(wrapBoolean(l > r));
                        case I64_LE_S -> pushInt(wrapBoolean(l <= r));
                        case I64_LT_S -> pushInt(wrapBoolean(l < r));
                        case I64_GE_U -> pushInt(wrapBoolean(Long.compareUnsigned(l,r) >= 0));
                        case I64_GT_U -> pushInt(wrapBoolean(Long.compareUnsigned(l,r) > 0));
                        case I64_LE_U -> pushInt(wrapBoolean(Long.compareUnsigned(l,r) <= 0));
                        case I64_LT_U -> pushInt(wrapBoolean(Long.compareUnsigned(l,r) < 0));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case IntBinaryInstruction b -> {
                    int l = popInt();
                    int r = popInt();
                    switch (b) {
                        case I32_ADD -> pushInt(l+r);
                        case I32_SUB -> pushInt(l-r);
                        case I32_MUL -> pushInt(l*r);
                        case I32_DIV_S -> pushInt(l/r);
                        case I32_REM_S -> pushInt(l%r);
                        case I32_DIV_U -> pushInt(Integer.divideUnsigned(l,r));
                        case I32_REM_U -> pushInt(Integer.remainderUnsigned(l,r));
                        case I32_MAX -> pushInt(Integer.max(l,r));
                        case I32_MIN -> pushInt(Integer.min(l,r));
                        case I32_AND -> pushInt(l&r);
                        case I32_OR -> pushInt(l|r);
                        case I32_XOR -> pushInt(l^r);
                        case I32_EQ -> pushInt(wrapBoolean(l == r));
                        case I32_NE -> pushInt(wrapBoolean(l != r));
                        case I32_GE_S -> pushInt(wrapBoolean(l >= r));
                        case I32_GT_S -> pushInt(wrapBoolean(l > r));
                        case I32_LE_S -> pushInt(wrapBoolean(l <= r));
                        case I32_LT_S -> pushInt(wrapBoolean(l < r));
                        case I32_GE_U -> pushInt(wrapBoolean(Integer.compareUnsigned(l,r) >= 0));
                        case I32_GT_U -> pushInt(wrapBoolean(Integer.compareUnsigned(l,r) > 0));
                        case I32_LE_U -> pushInt(wrapBoolean(Integer.compareUnsigned(l,r) <= 0));
                        case I32_LT_U -> pushInt(wrapBoolean(Integer.compareUnsigned(l,r) < 0));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case UnaryInstruction u -> {
                    switch (u) {
                        case DROP -> pop();
                        case I32_EQZ -> pushInt(wrapBoolean(popInt() == 0));
                        case I64_EQZ -> pushInt(wrapBoolean(pop() == 0));
                        case I32_POPCNT -> pushInt(Integer.bitCount(popInt()));
                        case I64_POPCNT -> push(Long.bitCount(pop()));
                        case I32_CLZ -> pushInt(Integer.numberOfLeadingZeros(popInt()));
                        case I64_CLZ -> push(Long.numberOfLeadingZeros(pop()));
                        case I32_CTZ -> pushInt(Integer.numberOfTrailingZeros(popInt()));
                        case I64_CTZ -> push(Long.numberOfTrailingZeros(pop()));
                        case F32_NEG -> pushFloat(-popFloat());
                        case F64_NEG -> pushDouble(-popDouble());
                        case F32_ABS -> pushFloat(Math.abs(popFloat()));
                        case F64_ABS -> pushDouble(Math.abs(popDouble()));
                        case F32_CEIL -> pushFloat((float) Math.ceil(popFloat()));
                        case F64_CEIL -> pushDouble(Math.ceil(popDouble()));
                        case F32_FLOOR -> pushFloat((float) Math.floor(popFloat()));
                        case F64_FLOOR -> pushDouble(Math.floor(popDouble()));
                        case F32_TRUNC -> {
                            float f = popFloat();
                            pushFloat((float) ((f < 0.0) ? Math.ceil(f) : Math.floor(f)));
                        }
                        case F64_TRUNC -> {
                            double f = popDouble();
                            pushDouble((f < 0.0) ? Math.ceil(f) : Math.floor(f));
                        }
                        case F32_DEMOTE_F64 -> pushFloat((float) popDouble());
                        case F64_PROMOTE_F32 -> pushDouble(popFloat());
                        case F32_NEAREST -> pushFloat(Math.round(popFloat()));
                        case F64_NEAREST -> pushDouble(Math.round(popDouble()));
                        case F32_SQRT -> pushFloat((float) Math.sqrt(popFloat()));
                        case F64_SQRT -> pushDouble(Math.sqrt(popDouble()));
                        // wrap and extend ops
                        case I32_WRAP_I64 -> pushInt(longToInt(pop()%(Integer.MAX_VALUE+1L)));
                        case I64_EXTEND_I32_S -> push(intToLong(popInt()));
                        case I64_EXTEND_I32_U -> push(Integer.toUnsignedLong(popInt()));
                        // Reinterpret ops
                        case I32_REINTERPRET_F32 -> pushInt(Float.floatToIntBits(popFloat()));
                        case F32_REINTERPRET_I32 -> pushFloat(Float.intBitsToFloat(popInt()));
                        case I64_REINTERPRET_F64 -> push(Double.doubleToLongBits(popDouble()));
                        case F64_REINTERPRET_I64 -> pushDouble(Double.longBitsToDouble(pop()));
                        // convert i32/i64 signed/unsigned to f32/f64
                        case F32_CONVERT_I32_S -> pushFloat(popInt());
                        case F32_CONVERT_I32_U -> pushFloat(Integer.toUnsignedLong(popInt()));
                        case F32_CONVERT_I64_S -> pushFloat(pop());
                        case F32_CONVERT_I64_U -> pushFloat(Float.parseFloat(Long.toUnsignedString(pop())));
                        case F64_CONVERT_I32_S -> pushDouble(popInt());
                        case F64_CONVERT_I32_U -> pushDouble(Integer.toUnsignedLong(popInt()));
                        case F64_CONVERT_I64_S -> pushDouble(pop());
                        case F64_CONVERT_I64_U -> pushDouble(Double.parseDouble(Long.toUnsignedString(pop())));
                        // convert f32/f64 to signed i32/i64
                        case I32_TRUNC_F32_S -> pushInt((int) popFloat());
                        case I32_TRUNC_F64_S -> pushInt((int) popDouble());
                        case I64_TRUNC_F32_S -> push((long) popFloat());
                        case I64_TRUNC_F64_S -> push((long) popDouble());
                        // convert f32/f64 to unsigned i32/i64
                        case I32_TRUNC_F32_U -> {
                            float f = popFloat();
                            if (f < 0.0f || f > Integer.MAX_VALUE) {
                                throw new RuntimeException("Integer overflow");
                            }
                            pushInt((int) f);
                        }
                        case I32_TRUNC_F64_U -> {
                            double d = popDouble();
                            long maxUnsignedInt = Integer.MAX_VALUE *2L+1;
                            if (d < 0.0 || d > maxUnsignedInt) {
                                throw new RuntimeException("Integer overflow");
                            }
                            pushInt( (int)((long) d));
                        }
                        case I64_TRUNC_F32_U -> {
                            float f = popFloat();
                            if (f < 0.0f || f > Long.MAX_VALUE) {
                                throw new RuntimeException("Integer overflow");
                            }
                            push(BigDecimal.valueOf(f).toBigInteger().longValue());
                        }
                        case I64_TRUNC_F64_U -> {
                            double d = popDouble();
                            if (d < 0.0 || d > Long.MAX_VALUE*2.0+1) {
                                throw new RuntimeException("Integer overflow");
                            }
                            push(BigDecimal.valueOf(d).toBigInteger().longValue());
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case StoreInstruction s -> {
                    byte[] data = switch (s) {
                        case I32_STORE -> intToBytes(popInt());
                        case I64_STORE -> longToBytes(pop());
                        case F32_STORE -> floatToBytes(popFloat());
                        case F64_STORE -> doubleToBytes(popDouble());
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    };
                    int addr = popInt();
                    store(addr, data);
                }
                case LoadInstruction l -> {
                    int addr = popInt();
                    switch (l) {
                        case I32_LOAD -> pushInt(bytesToInt(load(addr, Integer.BYTES)));
                        case I64_LOAD -> push(bytesToLong(load(addr, Long.BYTES)));
                        case F32_LOAD -> pushFloat(bytesToFloat(load(addr, Float.BYTES)));
                        case F64_LOAD -> pushDouble(bytesToDouble(load(addr, Double.BYTES)));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case FunctionInstruction f -> {
                    switch (f) {
                        case Call l -> {
                            Function fun = functions[l.val()];
                            Variable[] res = call(fun);
                            if (!fun.isVoidReturn()) {
                                // Push in reverse order
                                for (int i = res.length -1; i >= 0; i--) {
                                    pushVariable(res[i]);
                                }
                            }
                        }
                        case LocalGet l -> {
                            Variable var = locals[l.val()];
                            pushVariable(var);
                        }
                        case LocalSet l -> {
                            // What if we are setting new variable?
                            Variable var = locals[l.val()];
                            locals[l.val()] = Variable.newVariable(var.getType(), pop());
                        }
                        case LocalTee l -> {
                            long val = pop();
                            Variable var = locals[l.val()];
                            var = Variable.newVariable(var.getType(), val);
                            locals[l.val()] = var;
                            pushVariable(var);
                        }
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
        }
    }

    private void pushVariable(Variable var) {
        switch (var) {
            case F32Variable v -> pushFloat(v.val());
            case F64Variable v -> pushDouble(v.val());
            case I32Variable v -> pushInt(v.val());
            case I64Variable v -> push(v.val());
        }
    }

    public static Machine createAndExecute(Function[] functions, int memSize, Instruction[] instructions) {
        Machine m = new Machine(functions, memSize);
        m.execute(instructions, null);
        return m;
    }

    public static void main(String[] args) {
        Machine m = new Machine(null, 65536);
        Instruction[] ins = new Instruction[]{
                new DoubleConst(1.0)
        };
        m.execute(ins, null);
        System.out.println(m.popDouble());
    }
}
