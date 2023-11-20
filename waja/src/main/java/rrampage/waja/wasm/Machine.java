package rrampage.waja.wasm;

import java.util.ArrayDeque;
import java.util.Arrays;

import static rrampage.waja.utils.ConversionUtils.*;


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
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
        }
    }

    public static Machine createAndExecute(int memSize, Instruction[] instructions) {
        Machine m = new Machine(memSize);
        m.execute(instructions);
        return m;
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
