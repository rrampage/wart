package rrampage.wasp.vm;

import rrampage.wasp.data.*;
import rrampage.wasp.instructions.*;
import rrampage.wasp.utils.MathUtils;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.util.*;

import static rrampage.wasp.utils.ConversionUtils.*;


public class Machine {
    private static final int FUNC_LEVEL = -1;
    private static final int RETURN_LEVEL = -2;
    private static final int BLOCK_LEVEL = -3; // Signifies normal consumption of all instructions in code array
    private final MachineStack stack; // Store everything as long. Convert to type as per instruction

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
    private final ArrayDeque<String> callStack = new ArrayDeque<>();

    public Machine(Function[] functions, Table[] tables, Variable[] globals, int pages, DataSegment[] dataSegments, ElementSegment[] elementSegments, long startIdx) {
       this(functions, tables, globals, new Memory[]{new Memory(pages)}, dataSegments, elementSegments, null, startIdx, MachineVisitors.NULL_VISITOR);
    }

    public Machine(Function[] functions, Table[] tables, Variable[] globals, Memory[] memories, DataSegment[] dataSegments, ElementSegment[] elementSegments,
                   Map<String, Object> exportMap, long startIdx, MachineVisitor machineVisitor) {
        if (memories == null || memories.length == 0) {
            // Create a 1 page memory if null or zero-length memory is passed
            memories = new Memory[]{new Memory(1)};
        }
        this.stack = new MachineStack();
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

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public String stackView() {
        return stack.inspect();
    }

    private boolean isAligned(int align, int offset, int addr) {
        // TODO: 64 bit instruction can only support alignment of 4
        // (baseAddr + memarg.offset) mod 2^memarg.align == 0
        if (align > 8) {
            return false;
        }
        int effectiveAddr = addr + offset;
        return effectiveAddr%(1<<align) == 0;
    }

    private void call(Function fun) {
        callStack.push(fun.name());
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
        // System.out.println(STR."FUNC: \{fun.name()} Type: \{fun.type()}");
        try {
            execute(fun.code(), locals, FUNC_LEVEL);
        } catch (Exception e) {
            System.out.println(STR."Exception at \{fun.name()}\nJava stack trace: \{e.getMessage()}\nWASM call stack trace: \{callStack}");
            throw e;
        }
        if (machineVisitor.hasPostFunctionVisitor) {machineVisitor.visitPostFunction(fun);}
        callStack.pop();
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

    private void handleStack(int oldStackPtr, FunctionType type) {
        /*
            Handle correct stack pointer level. Drop extra args
            oldStackPtr = newStackPtr - numReturns
            if newStackPtr < oldStackPtr, CRASH
            if newStackPtr > oldStackPtr + numReturns
                drop all elements below delta while preserving top numReturn elems
         */
        int sp = stack.stackPointer();
        if (sp < oldStackPtr) {
            return;
            // throw new RuntimeException(STR."HANDLE_STACK_ERROR newSP \{sp} < oldSP \{oldStackPtr}");
        }
        // TODO: Check for sp < oldStackPtr + numReturn ??
        int keep = (type ==null) ? 0 : type.numReturns();
        int drop = sp - oldStackPtr - keep;
        if (drop <= 0 || keep == 0) {
            return;
        }
        // System.out.println(STR."HANDLE_STACK newSP \{sp} oldSP \{oldStackPtr} drop \{drop} keep \{keep}");
        stack.dropKeep(drop, keep);
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
                        case F64_COPY_SIGN -> pushDouble(Math.copySign(l,r));
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
                        case F32_COPY_SIGN -> pushFloat(Math.copySign(l,r));
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
                        case MEMORY_GROW -> {
                            int numPages = popInt();
                            int ret = getMainMemory().grow(numPages);
                            // System.out.println(STR."GROW_MEM numPages: \{numPages} return: \{ret}");
                            pushInt(ret);
                        }
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
                        case F32_NEAREST -> pushFloat(MathUtils.nearest(popFloat()));
                        case F64_NEAREST -> pushDouble(MathUtils.nearest(popDouble()));
                        case F32_SQRT -> pushFloat((float) Math.sqrt(popFloat()));
                        case F64_SQRT -> pushDouble(Math.sqrt(popDouble()));
                        // wrap and extend ops
                        case I32_WRAP_I64 -> pushInt((int)pop());
                        case I64_EXTEND_I32_S -> push(intToLong(popInt()));
                        case I64_EXTEND_I32_U -> push(Integer.toUnsignedLong(popInt()));
                        // Reinterpret ops
                        case I32_REINTERPRET_F32 -> pushInt(Float.floatToRawIntBits(popFloat()));
                        case F32_REINTERPRET_I32 -> pushFloat(Float.intBitsToFloat(popInt()));
                        case I64_REINTERPRET_F64 -> push(Double.doubleToRawLongBits(popDouble()));
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
                        case I32_TRUNC_F32_S, I32_TRUNC_SAT_F32_S -> pushInt((int) popFloat());
                        case I32_TRUNC_F64_S, I32_TRUNC_SAT_F64_S -> pushInt((int) popDouble());
                        case I64_TRUNC_F32_S, I64_TRUNC_SAT_F32_S -> push((long) popFloat());
                        case I64_TRUNC_F64_S, I64_TRUNC_SAT_F64_S -> push((long) popDouble());
                        // convert f32/f64 to unsigned i32/i64
                        case I32_TRUNC_F32_U, I32_TRUNC_SAT_F32_U -> pushInt(MathUtils.truncateFloatToUnsignedInt(popFloat()));
                        case I32_TRUNC_F64_U, I32_TRUNC_SAT_F64_U -> pushInt(MathUtils.truncateDoubleToUnsignedInt(popDouble()));
                        case I64_TRUNC_F32_U, I64_TRUNC_SAT_F32_U -> push(MathUtils.truncateFloatToUnsignedLong(popFloat()));
                        case I64_TRUNC_F64_U, I64_TRUNC_SAT_F64_U -> push(MathUtils.truncateDoubleToUnsignedLong(popDouble()));
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
                    long val = pop();
                    int addr = popInt();
                    int effectiveAddr = addr + s.offset();
                    // System.out.println(STR."addr: \{addr} offset:\{s.offset()} effectiveAddr: \{effectiveAddr} align: \{s.align()}");
                    switch (s) {
                        case StoreInstruction.I32Store _ -> getMainMemory().store(effectiveAddr, (int) val);
                        case StoreInstruction.I32Store8 _ -> getMainMemory().store(effectiveAddr,(byte)val);
                        case StoreInstruction.I32Store16 _ -> getMainMemory().store(effectiveAddr,(short)val);
                        case StoreInstruction.I64Store _ -> getMainMemory().store(effectiveAddr,val);
                        case StoreInstruction.I64Store8 _ -> getMainMemory().store(effectiveAddr,(byte)val);
                        case StoreInstruction.I64Store16 _ -> getMainMemory().store(effectiveAddr,(short)val);
                        case StoreInstruction.I64Store32 _, StoreInstruction.F32Store _ -> getMainMemory().store(effectiveAddr,(int) val);
                        case StoreInstruction.F64Store _ -> getMainMemory().store(effectiveAddr,val);
                    }
                }
                case LoadInstruction l -> {
                    int addr = popInt();
                    int effectiveAddr = addr + l.offset();
                    switch (l) {
                        case LoadInstruction.I32Load _ -> pushInt(getMainMemory().loadInt(effectiveAddr));
                        case LoadInstruction.I32Load8S _ -> pushInt(getMainMemory().loadByte(effectiveAddr));
                        case LoadInstruction.I32Load8U _ -> pushInt(Byte.toUnsignedInt(getMainMemory().loadByte(effectiveAddr)));
                        case LoadInstruction.I32Load16S _ -> pushInt(getMainMemory().loadShort(effectiveAddr));
                        case LoadInstruction.I32Load16U _ -> pushInt(Short.toUnsignedInt(getMainMemory().loadShort(effectiveAddr)));
                        case LoadInstruction.I64Load _ -> push(getMainMemory().loadLong(effectiveAddr));
                        case LoadInstruction.I64Load8S _ -> push(getMainMemory().loadByte(effectiveAddr));
                        case LoadInstruction.I64Load8U _ -> push(Byte.toUnsignedLong(getMainMemory().loadByte(effectiveAddr)));
                        case LoadInstruction.I64Load16S _ -> push(getMainMemory().loadShort(effectiveAddr));
                        case LoadInstruction.I64Load16U _ -> push(Short.toUnsignedLong(getMainMemory().loadShort(effectiveAddr)));
                        case LoadInstruction.I64Load32S _ -> push(getMainMemory().loadInt(effectiveAddr));
                        case LoadInstruction.I64Load32U _ -> push(Integer.toUnsignedLong(getMainMemory().loadInt(effectiveAddr)));
                        case LoadInstruction.F32Load _ -> pushInt(getMainMemory().loadInt(effectiveAddr));
                        case LoadInstruction.F64Load _ -> push(getMainMemory().loadLong(effectiveAddr));
                        default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                    }
                }
                case FunctionInstruction f -> {
                    switch (f) {
                        case FunctionInstruction.Call l -> call(functions[l.val()]);
                        case FunctionInstruction.CallIndirect l -> {
                            int tblOffset = popInt();
                            int tblIdx = l.tableIdx();
                            int typeIdx = l.typeIdx(); // TODO: Expose Types for another level of validation
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
                                    // System.out.println("Class for " + i + " is " + args[i].getClass());
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
                        case FunctionInstruction.LocalGet l -> push(locals[l.val()].getValAsLong());
                        case FunctionInstruction.LocalSet l -> locals[l.val()].setVal(pop());
                        case FunctionInstruction.LocalTee l -> {
                            long val = pop();
                            Variable var = locals[l.val()];
                            var.setVal(val);
                            push(val);
                            // pushVariable(var);
                        }
                    }
                }
                case GlobalInstruction i -> {
                    switch (i) {
                        case GlobalInstruction.GlobalGet g -> push(globals[g.val()].getValAsLong());
                        case GlobalInstruction.GlobalSet g -> globals[g.val()].setVal(pop());
                    }
                }
                case Select s -> {
                    // TODO: Type check for typed select by using vector of value types
                    int cmp = popInt();
                    long t2 = pop();
                    long t1 = pop();
                    long val = (cmp == 0) ? t2 : t1;
                    // System.out.printf("T1 %d T2: %d cmp: %d val: %d\n", t1, t2, cmp, val);
                    push(val);
                }
                case ControlFlowInstruction i -> {
                    int sp = stack.stackPointer();
                    switch (i) {
                        case ControlFlowInstruction.Block b -> {
                            level = execute(b.code(), locals, b.label());
                            if (level > b.label()) {
                                throw new RuntimeException("CONTROL_FLOW_ERROR");
                            }
                            handleStack(sp, b.type());
                            // If loop body ends normally or level is same as label of block
                            if (level == BLOCK_LEVEL || level == b.label()) {
                                continue;
                            }
                            machineVisitor.visitPostInstruction(ins);
                            return level;
                        }
                        case ControlFlowInstruction.Loop b -> {
                            do {
                                level = execute(b.code(), locals, b.label());
                            } while (level == b.label());
                            handleStack(sp, b.type());
                            // If loop body ends normally
                            if (level == BLOCK_LEVEL) {
                                continue;
                            }
                            machineVisitor.visitPostInstruction(ins);
                            return level;
                        }
                        case ControlFlowInstruction.Branch b -> {
                            // it has to jump to level pointed by the label
                            machineVisitor.visitPostInstruction(ins);
                            return b.label();
                        }
                        case ControlFlowInstruction.BranchIf b -> {
                            int cmp = popInt();
                            if (cmp != 0) {
                                machineVisitor.visitPostInstruction(ins);
                                return b.label();
                            }
                        }
                        case ControlFlowInstruction.BranchTable b -> {
                            int jmpIdx = popInt();
                            machineVisitor.visitPostInstruction(ins);
                            return (jmpIdx >= 0 && jmpIdx < b.labels().length) ? b.labels()[jmpIdx] : b.defaultLabel();
                        }
                        case ControlFlowInstruction.If b -> {
                            int cmp = popInt();
                            if (cmp == 0) {
                                continue;
                            }
                            level = execute(b.ifBlock(), locals, b.label());
                            handleStack(sp, b.type());
                            // If loop body ends normally
                            if (level == BLOCK_LEVEL || level == b.label()) {
                                continue;
                            }
                            machineVisitor.visitPostInstruction(ins);
                            return level;
                        }
                        case ControlFlowInstruction.IfElse b -> {
                            int cmp = popInt();
                            if (cmp != 0) {
                                level = execute(b.ifBlock(), locals, b.label());
                            } else {
                                level = execute(b.elseBlock(), locals, b.label());
                            }
                            handleStack(sp, b.type());
                            // If loop body ends normally
                            if (level == BLOCK_LEVEL || level == b.label()) {
                                continue;
                            }
                            machineVisitor.visitPostInstruction(ins);
                            return level;
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
                        case RefTypeInstruction.RefFunc r -> {
                            // TODO
                            System.out.println(STR."REF_FUNC \{r.functionIndex()}");
                            pushInt(r.functionIndex());
                        }
                        case RefTypeInstruction.RefIsNull _ -> pushInt(wrapBoolean(popInt() == Variable.REF_NULL));
                        case RefTypeInstruction.RefNull _ -> pushInt(Variable.REF_NULL);
                        case RefTypeInstruction.TableCopy r -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                        case RefTypeInstruction.TableFill r -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                        case RefTypeInstruction.TableGet r -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                        case RefTypeInstruction.TableGrow r -> {
                            int delta = popInt();
                            int funcIdx = popInt();
                            int ret = tables[r.tableIndex()].grow(delta, funcIdx);
                            pushInt(ret);
                        }
                        case RefTypeInstruction.TableInit r -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                        case RefTypeInstruction.TableSet r -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
                        case RefTypeInstruction.TableSize r -> pushInt(tables[r.tableIndex()].size());
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + ins.opCode());
            }
            machineVisitor.visitPostInstruction(ins);
        }
        return BLOCK_LEVEL;
    }

    private void pushVariable(Variable var) {
        push(var.getValAsLong());
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
        try {
            call(f);
        } finally {
            machineVisitor.end(this);
        }
    }

    public boolean compareStack(ConstInstruction... expected) {
        if (expected == null || expected.length == 0) {
            return true;
        }
        int n = expected.length;
        for (int i =0; i < n; i++) {
            var c = expected[n-i-1];
            switch (c) {
                case ConstInstruction.DoubleConst cc -> {
                    var v = popDouble();
                    if (Double.compare(v, cc.val()) != 0) {
                    System.out.println(STR."Expected: \{cc.val()} Found: \{v}");
                    return false;
                }}
                case ConstInstruction.FloatConst cc -> {
                    var v = popFloat();
                    if (Float.compare(v, cc.val()) != 0) {
                        System.out.println(STR."Expected: \{cc.val()} Found: \{v}");
                        return false;
                    }
                }
                case ConstInstruction.IntConst cc -> {
                    var v = popInt();
                    if (v != cc.val()) {
                        System.out.println(STR."Expected: \{cc.val()} Found: \{v}");
                        return false;
                    }
                }
                case ConstInstruction.LongConst cc -> {
                    var v = pop();
                    if (v != cc.val()) {
                        System.out.println(STR."Expected: \{cc.val()} Found: \{v}");
                        return false;
                    }
                }
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
