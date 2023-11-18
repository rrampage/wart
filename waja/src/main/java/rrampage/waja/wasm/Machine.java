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
                        case F64_EQ -> pushInt(wrapBoolean(l == r));
                        case F64_NE -> pushInt(wrapBoolean(l != r));
                        case F64_GE -> pushInt(wrapBoolean(l >= r));
                        case F64_GT -> pushInt(wrapBoolean(l > r));
                        case F64_LE -> pushInt(wrapBoolean(l <= r));
                        case F64_LT -> pushInt(wrapBoolean(l < r));
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
