package rrampage.waja.wasm;

import java.util.ArrayDeque;
import java.util.Arrays;

import static rrampage.waja.utils.ConversionUtils.wrapBoolean;


public class Machine {
    private ArrayDeque<Long> stack; // Store everything as long. Convert to type as per instruction
    private byte[] memory;

    public Machine(int memSize) {
       this.stack = new ArrayDeque<>(4096);
       this.memory = new byte[memSize];
    }

    public long pop() {
        return stack.pop();
    }

    public int popInt() {
        return stack.pop().intValue();
    }

    public float popFloat() {
        return Float.intBitsToFloat(stack.pop().intValue());
    }

    public double popDouble() {
        return Double.longBitsToDouble(stack.pop().longValue());
    }

    public void push(long val) {
        stack.push(val);
    }

    public void pushInt(int val) {
        stack.push((long) val);
    }

    public void pushFloat(float val) {
        stack.push((long) Float.floatToIntBits(val));
    }

    public void pushDouble(double val) {
        stack.push(Double.doubleToLongBits(val));
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

    public void execute(Instruction[] instructions) {
        // TODO
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
                        case F64_EQ -> pushInt(wrapBoolean(l == r));
                        case F64_NE -> pushInt(wrapBoolean(l != r));
                        case F64_GE -> pushInt(wrapBoolean(l >= r));
                        case F64_GT -> pushInt(wrapBoolean(l > r));
                        case F64_LE -> pushInt(wrapBoolean(l <= r));
                        case F64_LT -> pushInt(wrapBoolean(l < r));
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
                        case F32_EQ -> pushInt(wrapBoolean(l == r));
                        case F32_NE -> pushInt(wrapBoolean(l != r));
                        case F32_GE -> pushInt(wrapBoolean(l >= r));
                        case F32_GT -> pushInt(wrapBoolean(l > r));
                        case F32_LE -> pushInt(wrapBoolean(l <= r));
                        case F32_LT -> pushInt(wrapBoolean(l < r));
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
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
        }
    }

    public static void main(String[] args) {
        Machine m = new Machine(65536);
        Instruction[] ins = new Instruction[]{
                new DoubleConst(1.0)
        };
        m.execute(ins);
        System.out.println(m.popDouble());
    }
}
