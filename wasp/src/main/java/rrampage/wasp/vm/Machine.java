package rrampage.wasp.vm;

import rrampage.wasp.data.*;
import rrampage.wasp.instructions.*;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

import static rrampage.wasp.utils.ConversionUtils.*;


public class Machine {
    private static final int FUNC_LEVEL = -1;
    private static final int RETURN_LEVEL = -2;
    private final ArrayDeque<Long> stack; // Store everything as long. Convert to type as per instruction
    private final SequencedCollection<Long> stackView;

    // TODO : Keep in mind proposal for multiple memories:
    //  https://github.com/WebAssembly/multi-memory/blob/main/proposals/multi-memory/Overview.md
    private final Memory[] memories;
    private final Function[] functions;
    private final Table[] tables;
    private final Variable[] globals;
    private final DataSegment[] dataSegments;
    private final ElementSegment[] elementSegments;
    private final Map<String, Object> exportMap;
    private final long startIdx;
    private final MachineVisitor machineVisitor;

    public Machine(Function[] functions, Table[] tables, Variable[] globals, int pages, DataSegment[] dataSegments, ElementSegment[] elementSegments, long startIdx) {
       this(functions, tables, globals, new Memory[]{new Memory(pages)}, dataSegments, elementSegments, null, startIdx, MachineVisitors.instructionCountVisitor());
    }

    public Machine(Function[] functions, Table[] tables, Variable[] globals, Memory[] memories, DataSegment[] dataSegments, ElementSegment[] elementSegments,
                   Map<String, Object> exportMap, long startIdx, MachineVisitor machineVisitor) {
        if (memories == null || memories.length == 0) {
            // Create a 1 page memory if null or zero-length memory is passed
            memories = new Memory[]{new Memory(1)};
        }
        this.stack = new ArrayDeque<>(8192);
        this.stackView = Collections.unmodifiableSequencedCollection(this.stack.reversed());
        this.memories = memories;
        this.functions = functions;
        this.tables = tables;
        this.globals = globals;
       /*
              this stores level of the label of block
        */
        this.dataSegments = dataSegments;
        this.elementSegments = elementSegments;
        this.exportMap = exportMap;
        this.startIdx = startIdx;
        this.machineVisitor = machineVisitor;
        this.start();
        this.machineVisitor.start(this);
    }

    public Memory getMainMemory() {
        return this.memories[0];
    }

    public Map<String, Object> exports(){ return this.exportMap;}

    public Variable[] globals() { return this.globals;}

    public Function[] functions() { return this.functions;}

    public long pop() {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(StackOp.Pop.POP_LONG, stackView); }
        return stack.pop();
    }

    public int popInt() {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(StackOp.Pop.POP_INT, stackView); }
        return longToInt(stack.pop());
    }

    public float popFloat() {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(StackOp.Pop.POP_FLOAT, stackView); }
        return longToFloat(stack.pop());
    }

    public double popDouble() {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(StackOp.Pop.POP_DOUBLE, stackView); }
        return longToDouble(stack.pop());
    }

    public void push(long val) {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(new StackOp.PushLong(val), stackView); }
        stack.push(val);
    }

    public void pushInt(int val) {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(new StackOp.PushInt(val), stackView); }
        stack.push(intToLong(val));
    }

    public void pushFloat(float val) {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(new StackOp.PushFloat(val), stackView); }
        stack.push(floatToLong(val));
    }

    public void pushDouble(double val) {
        if (machineVisitor.hasStackVisitor) { machineVisitor.visitStack(new StackOp.PushDouble(val), stackView); }
        stack.push(doubleToLong(val));
    }

    public SequencedCollection<Long> stackView() {
        return stackView;
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

    private void call(Function fun) {
        if (machineVisitor.hasPreFunctionVisitor) {machineVisitor.visitPreFunction(fun);}
        // Creating a "scratch space" of variables for function params as well as local vars to be used in function body
        Variable[] locals = new Variable[fun.numParams() + fun.numLocals()];
        // LIFO for function params as params are pushed to stack and must be popped in reverse order
        for (int i = fun.numParams()-1; i >= 0; i-- ) {
            locals[i] = Variable.newMutableVariable(fun.type().paramTypes()[i], pop());
        }
        for (int i = fun.numParams(); i < locals.length; i++) {
            locals[i] = Variable.newMutableVariable(fun.locals()[i - fun.numParams()], 0);
        }
        // Set labels of machine to function labels and reset to original labels once execution is completed
        execute(fun.code(), locals, FUNC_LEVEL);
        if (fun.isVoidReturn()) {
            return;
        }
        int n = fun.type().returnTypes().length;
        if (machineVisitor.hasPostFunctionVisitor) {machineVisitor.visitPostFunction(fun);}
    }

    private void start() {
        if (startIdx < 0 || startIdx >= functions.length) {
            return;
        }
        // Verify that it takes no params and returns none
        Function startFun = functions[(int) startIdx];
        if (!startFun.isVoidReturn() && startFun.numParams() > 0) {
            return;
        }
        execute(new Instruction[]{new FunctionInstruction.Call((int) startIdx)}, null, FUNC_LEVEL);
    }

    private int execute(Instruction[] instructions, Variable[] locals, int level) {
        for (Instruction ins : instructions) {
            machineVisitor.visitPreInstruction(ins);
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
                    // System.out.printf("Ins: %s l : %d r : %d\n", b, l ,r);
                    switch (b) {
                        case I32_ADD -> pushInt(l+r);
                        case I32_SUB -> pushInt(l-r);
                        case I32_MUL -> pushInt(l*r);
                        case I32_DIV_S -> pushInt(l/r);
                        case I32_REM_S -> pushInt(l%r);
                        case I32_DIV_U -> pushInt(Integer.divideUnsigned(l,r));
                        case I32_REM_U -> pushInt(Integer.remainderUnsigned(l,r));
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
                        case I32_EXTEND8_S -> pushInt((byte) popInt());
                        case I32_EXTEND16_S -> pushInt((short) popInt());
                        case I64_EXTEND8_S -> push((byte) pop());
                        case I64_EXTEND16_S -> push((short) pop());
                        case I64_EXTEND32_S -> push((int) pop());
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
                        // System.out.println("Aligned write for " + s.align() + " " + s.opCode());
                        getMainMemory().store(effectiveAddr, data);
                    } else {
                        // System.out.println("Unaligned write for " + s.align() + " " + s.opCode());
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
                        case FunctionInstruction.Call l -> call(functions[l.val()]);
                        case FunctionInstruction.CallIndirect l -> {
                            int tblOffset = popInt();
                            int tblIdx = l.idx();
                            if (tblOffset < 0 || tblIdx < 0 || tblIdx >= tables.length || tblOffset >= tables[tblIdx].size()) {
                                throw new RuntimeException("Array bounds mismatch in indirect call");
                            }
                            Function fun = tables[tblIdx].get(tblOffset);
                            if (fun == null) {
                                throw new RuntimeException("Function Type mismatch in indirect call");
                            }
                            call(fun);
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
                                        case Variable.FuncrefVariable x -> x.getVal();
                                    };
                                    System.out.println("Class for " + i + " is " + args[i].getClass());
                                }
                                Object ret = function.invokeWithArguments(args);
                                if (type.isVoidReturn()) {
                                    continue;
                                }
                                if (type.returnTypes().length == 1) {
                                    // push ret to Stack
                                    ValueType retType = type.returnTypes()[0];
                                    switch (retType) {
                                        case ValueType.NumType.I32 -> pushInt((int) ret);
                                        case ValueType.NumType.I64 -> push((long) ret);
                                        case ValueType.NumType.F32 -> pushFloat((float) ret);
                                        case ValueType.NumType.F64 -> pushDouble((double) ret);
                                        default -> throw new IllegalStateException(String.format("Invalid value type: %s", retType));
                                    }
                                    continue;
                                }
                                if (!ret.getClass().isArray()) {
                                    throw new RuntimeException("Invalid class returned for import function: %s".formatted(ret.getClass()));
                                }
                                // Push in reverse order
                                for (int i = type.returnTypes().length -1; i >= 0; i--) {
                                    switch (type.returnTypes()[i]) {
                                        case ValueType.NumType.I32 -> pushInt((int) Array.get(ret, i));
                                        case ValueType.NumType.I64 -> push((long) Array.get(ret, i));
                                        case ValueType.NumType.F32 -> pushFloat((float) Array.get(ret, i));
                                        case ValueType.NumType.F64 -> pushDouble((double) Array.get(ret, i));
                                        default -> throw new IllegalStateException(String.format("Invalid value type: %s", type.returnTypes()[i]));
                                    }
                                }
                            } catch (Throwable e) {
                                System.err.println(e.getMessage());
                                throw new RuntimeException("InvokeError " + e.getMessage());
                            }
                        }
                        case FunctionInstruction.Return() -> {
                            machineVisitor.visitPostInstruction(ins);
                            return RETURN_LEVEL;
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
                            // System.out.println(Variable.debug(globals[g.val()]));
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
                    var currLevel = level;
                    switch (i) {
                        case ControlFlowInstruction.Block b -> {
                            level = execute(b.code(), locals, b.label());
                            if (level == b.label()) {
                                machineVisitor.visitPostInstruction(ins);
                                return level-1;
                            }
                        }
                        case ControlFlowInstruction.Loop b -> {
                            do {
                                level = execute(b.code(), locals, b.label());
                            } while (level == b.label());
                        }
                        case ControlFlowInstruction.Branch b -> {
                            // it has to jump to level pointed by the label
                            machineVisitor.visitPostInstruction(ins);
                            return b.label();
                        }
                        case ControlFlowInstruction.BranchIf b -> {
                            int cmp = popInt();
                            if (cmp == 1) {
                                machineVisitor.visitPostInstruction(ins);
                                return b.label();
                            }
                        }
                        case ControlFlowInstruction.BranchTable b -> {
                            int jmpIdx = popInt();
                            machineVisitor.visitPostInstruction(ins);
                            return (jmpIdx < b.labels().length) ? b.labels()[jmpIdx] : b.defaultLabel();
                        }
                        case ControlFlowInstruction.If b -> {
                            int cmp = popInt();
                            if (cmp == 1) {
                                level = execute(b.ifBlock(), locals, b.label());
                            }
                            if (currLevel != level) {
                                machineVisitor.visitPostInstruction(ins);
                                return level;
                            }
                        }
                        case ControlFlowInstruction.IfElse b -> {
                            int cmp = popInt();
                            if (cmp == 1) {
                                level = execute(b.ifBlock(), locals, b.label());
                            } else {
                                level = execute(b.elseBlock(), locals, b.label());
                            }
                            if (currLevel != level) {
                                machineVisitor.visitPostInstruction(ins);
                                return level;
                            }
                        }
                        case ControlFlowInstruction.Else _else -> {} // Do nothing
                        case ControlFlowInstruction.End _end -> {} // Do nothing
                    }
                }
                case SegmentInstruction i -> {
                    switch (i) {
                        case SegmentInstruction.DataDrop(int segIdx) -> this.dataSegments[segIdx] = new DataSegment.PassiveDataSegment(new byte[0]);
                        case SegmentInstruction.MemoryCopy(int srcMemIdx, int dstMemIdx) -> {
                            Memory srcMem = memories[srcMemIdx], dstMem = memories[dstMemIdx];
                            int numBytesToCopy = popInt();
                            int srcAddr = popInt();
                            int dstAddr = popInt();
                            // TODO : Implement Copy without intermediate byte[]
                            byte[] data = srcMem.load(srcAddr, numBytesToCopy);
                            dstMem.store(dstAddr, data);
                        }
                        case SegmentInstruction.MemoryFill(int memIdx) -> {
                            int numBytesToSet = popInt();
                            byte val = (byte) popInt();
                            int dstAddr = popInt();
                            memories[memIdx].fill(dstAddr, val, numBytesToSet);
                        }
                        case SegmentInstruction.MemoryInit(int segIdx, int memIdx) -> {
                            Memory m = memories[memIdx];
                            DataSegment seg = dataSegments[segIdx];
                            if (seg instanceof DataSegment.ActiveDataSegment) {
                                throw new IllegalStateException("Can not load data from active segment at index: " + segIdx);
                            }
                            int numBytesToCopy = popInt();
                            int srcOffset = popInt();
                            int dstAddr = popInt();
                            m.store(dstAddr, seg.data(), srcOffset, numBytesToCopy);
                        }
                    }
                }
                case RefTypeInstruction i -> {
                    switch (i) {
                        case RefTypeInstruction.ElemDrop(int elemIdx) -> {
                            // TODO : Should we create a zero-byte elem segment here and preserve reftype info ?
                            this.elementSegments[elemIdx] = null;
                        }
                        case RefTypeInstruction.RefFunc r -> {}
                        case RefTypeInstruction.RefIsNull r -> {}
                        case RefTypeInstruction.RefNull r -> {}
                        case RefTypeInstruction.TableCopy r -> {}
                        case RefTypeInstruction.TableFill r -> {}
                        case RefTypeInstruction.TableGet r -> {}
                        case RefTypeInstruction.TableGrow r -> {}
                        case RefTypeInstruction.TableInit r -> {}
                        case RefTypeInstruction.TableSet r -> {}
                        case RefTypeInstruction.TableSize r -> {}
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
            machineVisitor.visitPostInstruction(ins);
        }
        return level-1;
    }

    private void pushVariable(Variable var) {
        switch (var) {
            case Variable.F32Variable v -> pushFloat(v.getVal());
            case Variable.F64Variable v -> pushDouble(v.getVal());
            case Variable.I32Variable v -> pushInt(v.getVal());
            case Variable.I64Variable v -> push(v.getVal());
            case Variable.FuncrefVariable v -> push(v.getVal());
        }
    }

    public void invoke(String function, ConstInstruction... expr) {
        Object o = exportMap.get(function);
        if (!(o instanceof Function f)) {
            throw new RuntimeException("Invalid WASM export called: " + function);
        }
        var type = f.type();
        expr = (expr == null) ? new ConstInstruction[]{} : expr;
        if (type.numParams() != expr.length) {
            throw new RuntimeException(String.format("INVOKE: Incorrect number of params passed for %s. Expected: %d Got: %d", function, type.numParams(), expr.length));
        }
        // machineVisitor.start(this);
        execute(expr, null, FUNC_LEVEL);
        call(f);
        machineVisitor.end(this);
    }

    public boolean compareStack(ConstInstruction... expected) {
        if (expected == null || expected.length == 0) {
            return true;
        }
        int n = expected.length;
        for (int i =0; i < n; i++) {
            var c = expected[n-i-1];
            Instruction eq = switch (c) {
                case ConstInstruction.DoubleConst cc -> DoubleBinaryInstruction.F64_EQ;
                case ConstInstruction.FloatConst cc -> FloatBinaryInstruction.F32_EQ;
                case ConstInstruction.IntConst cc -> IntBinaryInstruction.I32_EQ;
                case ConstInstruction.LongConst cc -> LongBinaryInstruction.I64_EQ;
            };
            var checkFun = new Function("__check_"+i, FunctionType.VOID, null, Instruction.of(c, eq));
            call(checkFun);
            if (popInt() != 1) {
                return false;
            }
        }
        return true;
    }

    public static Machine createAndStart(Function[] functions, Table[] tables, Variable[] globals, int pages, DataSegment[] dataSegments, ElementSegment[] elementSegments, long startIdx) {
        Machine m = new Machine(functions, tables, globals, pages, dataSegments, elementSegments, startIdx);
        m.machineVisitor.end(m);
        return m;
    }
}
