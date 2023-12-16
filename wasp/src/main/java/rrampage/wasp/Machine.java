package rrampage.wasp;

import rrampage.wasp.data.*;
import rrampage.wasp.instructions.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Arrays;

import static rrampage.wasp.utils.ConversionUtils.*;


public class Machine {
    private final ArrayDeque<Long> stack; // Store everything as long. Convert to type as per instruction

    // TODO : Keep in mind proposal for multiple memories:
    //  https://github.com/WebAssembly/multi-memory/blob/main/proposals/multi-memory/Overview.md
    private Memory[] memories;
    private final Function[] functions;
    private final Table[] tables;
    private final Variable[] globals;
    private int[] labels;
    private final long startIdx;

    public Machine(Function[] functions, Table[] tables, Variable[] globals, int pages, long startIdx) {
       this(functions, tables, globals, new Memory[]{new Memory(pages)}, startIdx);
    }

    public Machine(Function[] functions, Table[] tables, Variable[] globals, Memory[] memories, long startIdx) {
        if (memories == null || memories.length == 0) {
            throw new RuntimeException("Null or zero size memories during init");
        }
        this.stack = new ArrayDeque<>(8192);
        this.memories = memories;
        this.functions = functions;
        this.tables = tables;
        this.globals = globals;
       /*
              this stores level of the label of block
        */
        this.labels = new int[]{0, -1, -1, -1, -1, -1};
        this.startIdx = startIdx;
    }

    public Memory getMainMemory() {
        return this.memories[0];
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

    public void printStack() {
        Long[] arr = new Long[stack.size()];
        stack.toArray(arr);
        System.out.println(" Stack: " + Arrays.toString(arr));
    }

    private boolean isAligned(int align, int offset, int addr) {
        // TODO: 64 bit instruction can only support alignment of 4
        // (baseAddr + memarg.offset) mod 2^memarg.align == 0
        if (align > 4) {
            return false;
        }
        int effectiveAddr = addr + offset;
        return effectiveAddr%(1<<align) == 0;
    }

    public Variable[] call(Function fun, int level) {
        // Creating a "scratch space" of variables for function params as well as local vars to be used in function body
        Variable[] locals = new Variable[fun.numParams() + fun.numLocals()];
        // LIFO for function params as params are pushed to stack and must be popped in reverse order
        for (int i = fun.numParams()-1; i >= 0; i-- ) {
            locals[i] = Variable.newVariable(fun.type().paramTypes()[i], pop());
        }
        for (int i = fun.numParams(); i < locals.length; i++) {
            locals[i] = Variable.newVariable(fun.locals()[i - fun.numParams()], 0);
        }
        int[] oldLabels = Arrays.copyOf(labels, labels.length);
        // Set labels of machine to function labels and reset to original labels once execution is completed
        this.labels = fun.labels();
        execute(fun.code(), locals, level);
        this.labels = oldLabels;
        if (fun.isVoidReturn()) {
            return null;
        }
        int n = fun.type().returnTypes().length;
        Variable[] returns = new Variable[n];
        for (int i = 0; i < n; i++) {
            returns[i] = Variable.newVariable(fun.type().returnTypes()[i], pop());
        }
        return returns;
    }

    public void start() {
        if (startIdx < 0 || startIdx >= functions.length) {
            return;
        }
        // Verify that it takes no params and returns none
        Function startFun = functions[(int) startIdx];
        if (!startFun.isVoidReturn() && startFun.numParams() > 0) {
            return;
        }
        execute(new Instruction[]{new FunctionInstruction.Call((int) startIdx)}, null, 0);
    }

    public int execute(Instruction[] instructions, Variable[] locals, int level) {
        for (Instruction ins : instructions) {
            System.out.println("Instruction: " + ins.opCode());
            printStack();
            switch (ins) {
                case ConstInstruction.DoubleConst c -> pushDouble(c.val());
                case ConstInstruction.FloatConst c -> pushFloat(c.val());
                case ConstInstruction.IntConst c -> pushInt(c.val());
                case ConstInstruction.LongConst c -> push(c.val());
                case DoubleBinaryInstruction b -> {
                    double r = popDouble();
                    double l = popDouble();
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
                    float r = popFloat();
                    float l = popFloat();
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
                    long r = pop();
                    long l = pop();
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
                        case I64_SHL -> push(l << r);
                        case I64_SHR_S -> push(l >> r);
                        case I64_SHR_U -> push(l >>> r);
                        case I64_ROTL -> push(Long.rotateLeft(l, (int) r));
                        case I64_ROTR -> push(Long.rotateRight(l, (int) r));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case IntBinaryInstruction b -> {
                    int r = popInt();
                    int l = popInt();
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
                        case I32_SHL -> pushInt(l << r);
                        case I32_SHR_S -> push(l >> r);
                        case I32_SHR_U -> pushInt(l >>> r);
                        case I32_ROTL -> pushInt(Integer.rotateLeft(l, r));
                        case I32_ROTR -> pushInt(Integer.rotateRight(l, r));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case UnaryInstruction u -> {
                    switch (u) {
                        case DROP -> pop();
                        case MEMORY_GROW -> pushInt(getMainMemory().grow(popInt()));
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
                case NullaryInstruction u -> {
                    switch (u) {
                        case UNREACHABLE -> throw new RuntimeException("Unreachable op code detected! Crashing on demand!");
                        case NOP -> {}
                        case MEMORY_SIZE -> pushInt(getMainMemory().getMemorySize());
                    }
                }
                case StoreInstruction s -> {
                    byte[] data = switch (s) {
                        case StoreInstruction.I32Store i -> intToBytes(popInt());
                        case StoreInstruction.I32Store8 i -> new byte[]{(byte)popInt()};
                        case StoreInstruction.I32Store16 i -> shortToBytes((short)popInt());
                        case StoreInstruction.I64Store i -> longToBytes(pop());
                        case StoreInstruction.I64Store8 i -> new byte[]{(byte)pop()};
                        case StoreInstruction.I64Store16 i -> shortToBytes((short)pop());
                        case StoreInstruction.I64Store32 i -> intToBytes((int) pop());
                        case StoreInstruction.F32Store i -> floatToBytes(popFloat());
                        case StoreInstruction.F64Store i -> doubleToBytes(popDouble());
                    };
                    int addr = popInt();
                    int effectiveAddr = addr + s.offset();
                    if (isAligned(s.align(), s.offset(), effectiveAddr)) {
                        System.out.println("Aligned write for " + s.align() + " " + s.opCode());
                        getMainMemory().store(effectiveAddr, data);
                    } else {
                        System.out.println("Unaligned write for " + s.align() + " " + s.opCode());
                        getMainMemory().store(addr, data);
                    }
                }
                case LoadInstruction l -> {
                    int addr = popInt();
                    int effectiveAddr = addr + l.offset();
                    int loadAddr = isAligned(l.align(), l.offset(), effectiveAddr) ? effectiveAddr : addr;
                    switch (l) {
                        case LoadInstruction.I32Load i -> pushInt(bytesToInt(getMainMemory().load(loadAddr, Integer.BYTES)));
                        case LoadInstruction.I32Load8S i -> pushInt(getMainMemory().load(loadAddr, Byte.BYTES)[0]);
                        case LoadInstruction.I32Load8U i -> pushInt(Byte.toUnsignedInt(getMainMemory().load(loadAddr, Byte.BYTES)[0]));
                        case LoadInstruction.I32Load16S i -> pushInt(bytesToShort(getMainMemory().load(loadAddr, Short.BYTES)));
                        case LoadInstruction.I32Load16U i -> pushInt(Short.toUnsignedInt(bytesToShort(getMainMemory().load(loadAddr, Short.BYTES))));
                        case LoadInstruction.I64Load i -> push(bytesToLong(getMainMemory().load(loadAddr, Long.BYTES)));
                        case LoadInstruction.I64Load8S i -> push(getMainMemory().load(loadAddr, Byte.BYTES)[0]);
                        case LoadInstruction.I64Load8U i -> push(Byte.toUnsignedLong(getMainMemory().load(loadAddr, Byte.BYTES)[0]));
                        case LoadInstruction.I64Load16S i -> push(bytesToShort(getMainMemory().load(loadAddr, Short.BYTES)));
                        case LoadInstruction.I64Load16U i -> push(Short.toUnsignedLong(bytesToShort(getMainMemory().load(loadAddr, Short.BYTES))));
                        case LoadInstruction.I64Load32S i -> push(bytesToInt(getMainMemory().load(loadAddr, Integer.BYTES)));
                        case LoadInstruction.I64Load32U i -> push(Integer.toUnsignedLong(bytesToInt(getMainMemory().load(loadAddr, Integer.BYTES))));
                        case LoadInstruction.F32Load i -> pushFloat(bytesToFloat(getMainMemory().load(loadAddr, Float.BYTES)));
                        case LoadInstruction.F64Load i -> pushDouble(bytesToDouble(getMainMemory().load(loadAddr, Double.BYTES)));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case FunctionInstruction f -> {
                    switch (f) {
                        case FunctionInstruction.Call l -> {
                            Function fun = functions[l.val()];
                            Variable[] res = call(fun, level);
                            if (!fun.isVoidReturn()) {
                                // Push in reverse order
                                for (int i = res.length -1; i >= 0; i--) {
                                    pushVariable(res[i]);
                                }
                            }
                        }
                        case FunctionInstruction.CallIndirect l -> {
                            int tblOffset = popInt();
                            int tblIdx = l.tableIdx();
                            if (tblOffset < 0 || tblIdx < 0 || tblIdx >= tables.length || tblOffset >= tables[tblIdx].size()) {
                                throw new RuntimeException("Array bounds mismatch in indirect call");
                            }
                            Function fun = tables[tblIdx].get(tblOffset);
                            if (fun == null || !fun.type().equals(l.type())) {
                                throw new RuntimeException("Function Type mismatch in indirect call");
                            }
                            Variable[] res = call(fun, level);
                            if (!fun.isVoidReturn()) {
                                // Push in reverse order
                                for (int i = res.length -1; i >= 0; i--) {
                                    pushVariable(res[i]);
                                }
                            }
                        }
                        case FunctionInstruction.CallJava(FunctionType type, MethodHandle function) -> {
                            try {
                                Object[] args = new Object[type.numParams()];
                                for (int  i = 0; i < type.numParams(); i++) {
                                    Variable v = locals[i];
                                    args[i] = switch (v) {
                                        case Variable.F32Variable x -> x.getVal();
                                        case Variable.F64Variable x -> x.getVal();
                                        case Variable.I32Variable x -> x.getVal();
                                        case Variable.I64Variable x -> x.getVal();
                                    };
                                    System.out.println("Class for " + i + " is " + args[i].getClass());
                                }
                                Object ret = function.invokeWithArguments(args);
                                if (type.isVoidReturn()) {
                                    continue;
                                }
                                if (type.returnTypes().length == 1) {
                                    // push ret to Stack
                                    ValueType.NumType retType = type.returnTypes()[0];
                                    switch (retType) {
                                        case I32 -> pushInt((int) ret);
                                        case I64 -> push((long) ret);
                                        case F32 -> pushFloat((float) ret);
                                        case F64 -> pushDouble((double) ret);
                                    }
                                    continue;
                                }
                                if (!ret.getClass().isArray()) {
                                    throw new RuntimeException("Invalid class returned for import function: %s".formatted(ret.getClass()));
                                }
                                // Push in reverse order
                                for (int i = type.returnTypes().length -1; i >= 0; i--) {
                                    switch (type.returnTypes()[i]) {
                                        case I32 -> pushInt((int) Array.get(ret, i));
                                        case I64 -> push((long) Array.get(ret, i));
                                        case F32 -> pushFloat((float) Array.get(ret, i));
                                        case F64 -> pushDouble((double) Array.get(ret, i));
                                    }
                                }
                            } catch (Throwable e) {
                                System.err.println(e.getMessage());
                                e.printStackTrace();
                                throw new RuntimeException("InvokeError " + e.getMessage());
                            }
                        }
                        case FunctionInstruction.Return() -> {
                            return level;
                        }
                        case FunctionInstruction.LocalGet l -> {
                            Variable var = locals[l.val()];
                            pushVariable(var);
                        }
                        case FunctionInstruction.LocalSet l -> {
                            Variable var = locals[l.val()];
                            var.setVal(pop());
                        }
                        case FunctionInstruction.LocalTee l -> {
                            long val = pop();
                            Variable var = locals[l.val()];
                            var.setVal(val);
                            pushVariable(var);
                        }
                    }
                }
                case GlobalInstruction i -> {
                    switch (i) {
                        case GlobalInstruction.GlobalGet g -> {
                            Variable var = globals[g.val()];
                            pushVariable(var);
                        }
                        case GlobalInstruction.GlobalSet g -> {
                            Variable var = globals[g.val()];
                            var.setVal(pop());
                            System.out.println(Variable.debug(globals[g.val()]));
                        }
                    }
                }
                case Select() -> {
                    int cmp = popInt();
                    long t1 = pop();
                    long t2 = pop();
                    push((cmp == 0) ? t1 : t2);
                }
                case ControlFlowInstruction i -> {
                    // TODO - Verify block instruction
                    switch (i) {
                        case ControlFlowInstruction.Block b -> {
                            labels[b.label()] = level;
                            level = execute(b.code(), locals, level+1);
                        }
                        case ControlFlowInstruction.Loop b -> {
                            int currLevel = level;
                            labels[b.label()] = level;
                            do {
                                System.out.println("Level:" + level + ", currLevel: " + currLevel);
                                level = execute(b.code(), locals, currLevel+1);
                                System.out.println("Level:" + level + ", currLevel: " + currLevel);
                            } while (level != currLevel + 1);
                        }
                        case ControlFlowInstruction.Branch b -> {
                            // it has to jump to level pointed by the label
                            return labels[b.label()];
                        }
                        case ControlFlowInstruction.BranchIf b -> {
                            int cmp = popInt();
                            System.out.println("Branch If - " + (cmp == 1));
                            if (cmp == 1) {
                                return labels[b.label()];
                            }
                        }
                        case ControlFlowInstruction.BranchTable b -> {
                            int jmpIdx = popInt();
                            if (jmpIdx < b.labels().length) {
                                return labels[b.labels()[jmpIdx]];
                            } else {
                                return labels[b.defaultLabel()];
                            }
                        }
                        case ControlFlowInstruction.If b -> {
                            int cmp = popInt();
                            if (cmp == 1) {
                                execute(b.ifBlock(), locals, level);
                            }
                        }
                        case ControlFlowInstruction.IfElse b -> {
                            int cmp = popInt();
                            if (cmp == 1) {
                                execute(b.ifBlock(), locals, level);
                            } else {
                                execute(b.elseBlock(), locals, level);
                            }
                        }
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
        }
        return level;
    }

    private void pushVariable(Variable var) {
        switch (var) {
            case Variable.F32Variable v -> pushFloat(v.getVal());
            case Variable.F64Variable v -> pushDouble(v.getVal());
            case Variable.I32Variable v -> pushInt(v.getVal());
            case Variable.I64Variable v -> push(v.getVal());
        }
    }

    public static Machine createAndStart(Function[] functions, Table[] tables, Variable[] globals, int pages, long startIdx) {
        Machine m = new Machine(functions, tables, globals, pages, startIdx);
        m.start();
        return m;
    }

    public static void main(String[] args) {
        Machine m = new Machine(null,  null, null, 1, -1);
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.DoubleConst(1.0)
        };
        m.execute(ins, null, 0);
        System.out.println(m.popDouble());
    }
}
