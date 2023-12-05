package rrampage.waja.wasm;

import org.junit.Test;
import rrampage.waja.wasm.data.DataType;
import rrampage.waja.wasm.data.Function;

import static org.junit.Assert.assertEquals;
import static rrampage.waja.utils.ConversionUtils.*;

public class MachineTest {
    private static final int MEM_PAGES = 1;

    @Test
    public void shouldPushConst() {
        Instruction[] ins = new Instruction[]{
                new FloatConst(2.0f),
                new DoubleConst(1.0),
                new IntConst(4),
                new LongConst(1112345345667L),
        };
        Machine m = Machine.createAndExecute(null, null, MEM_PAGES, ins);
        assertEquals(1112345345667L, m.pop());
        assertEquals(4, m.popInt());
        assertEquals(1.0, m.popDouble(), 0.0);
        assertEquals(2.0f, m.popFloat(), 0.0f);
    }

    @Test
    public void shouldDrop() {
        Instruction[] ins = new Instruction[]{
                new IntConst(4),
                new LongConst(1112345345667L),
                UnaryInstruction.DROP,
        };
        Machine m = Machine.createAndExecute(null, null, MEM_PAGES, ins);
        assertEquals(4, m.popInt());
    }

    @Test
    public void shouldAdd() {
        double l = 123.0;
        double r = 345.0;
        float fl = 223.5f;
        float fr = -223.5f;
        long ll = 1234567890;
        long lr = 1111111111;
        int il = 11111;
        int ir = 1111111;
        Instruction[] ins = new Instruction[]{
                new DoubleConst(l),
                new DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new FloatConst(fl),
                new FloatConst(fr),
                FloatBinaryInstruction.F32_ADD,
                new IntConst(il),
                new IntConst(ir),
                IntBinaryInstruction.I32_ADD,
                new LongConst(ll),
                new LongConst(lr),
                LongBinaryInstruction.I64_ADD,
        };
        Machine m = Machine.createAndExecute(null, null, MEM_PAGES, ins);
        assertEquals(ll+lr, m.pop());
        assertEquals(il+ir, m.popInt());
        assertEquals(0.0f, m.popFloat(), 0.0f);
        assertEquals(m.popDouble(), l + r, 0.0);
    }

    @Test
    public void shouldAddAndCompare() {
        double l = 123.0;
        double r = 345.0;
        double a = l+r;
        float fl = 223.5f;
        float fr = -223.5f;
        float fa = fl + fr;
        long ll = 1234567890;
        long lr = 1111111111;
        long la = ll + lr;
        int il = 11111;
        int ir = 1111111;
        int ia = il + ir;
        Instruction[] ins = new Instruction[]{
                new DoubleConst(l),
                new DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new DoubleConst(a),
                DoubleBinaryInstruction.F64_EQ,
                new FloatConst(fl),
                new FloatConst(fr),
                FloatBinaryInstruction.F32_ADD,
                new FloatConst(fa),
                FloatBinaryInstruction.F32_EQ,
                new IntConst(il),
                new IntConst(ir),
                IntBinaryInstruction.I32_ADD,
                new IntConst(ia),
                IntBinaryInstruction.I32_EQ,
                new LongConst(ll),
                new LongConst(lr),
                LongBinaryInstruction.I64_ADD,
                new LongConst(la),
                LongBinaryInstruction.I64_EQ,
        };
        Machine m = Machine.createAndExecute(null, null, MEM_PAGES, ins);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
    }

    @Test
    public void shouldStoreAndLoad() {
        int iAddr = 8;
        int fAddr = iAddr+4;
        int lAddr = fAddr+8;
        int dAddr = lAddr+8;
        int sAddr = dAddr+8;
        int i = 1111, s = 100000;
        long l = -1111111111L;
        float f = 111111.0f;
        double d = 1111.0;
        Instruction[] ins = new Instruction[]{
                new IntConst(dAddr),
                new DoubleConst(d),
                new IntConst(lAddr),
                new LongConst(l),
                new IntConst(fAddr),
                new FloatConst(f),
                new IntConst(iAddr),
                new IntConst(i),
                new IntConst(sAddr),
                new IntConst(s),
                new I32Store16(1, 0),
                new I32Store(0, 0),
                new F32Store(0, 0),
                new I64Store(0, 0),
                new F64Store(0, 0),
                new IntConst(dAddr),
                new F64Load(0, 0),
                new IntConst(lAddr),
                new I64Load(0, 0),
                new IntConst(fAddr),
                new F32Load(0, 0),
                new IntConst(iAddr),
                new I32Load(0,0),
                new IntConst(sAddr),
                new I32Load16U(0, 0),
        };
        Machine m = Machine.createAndExecute(null, null, MEM_PAGES, ins);
        assertEquals(m.popInt(), s%65536);
        assertEquals(m.popInt(), i);
        assertEquals(m.popFloat(), f, 0.0f);
        assertEquals(m.pop(), l);
        assertEquals(m.popDouble(), d, 0.0);
        long x = Short.toUnsignedLong(bytesToShort(m.load(sAddr, 2)));
        assertEquals(x, s%65536);
    }

    @Test
    public void shouldCallIntConstFunction() {
        int i = 42;
        Function fun = new Function(null, null, new DataType[]{DataType.I32}, new Instruction[]{new IntConst(i)});
        Instruction[] ins = new Instruction[] {
          new Call(0)
        };
        Machine m = Machine.createAndExecute(new Function[]{fun}, null, MEM_PAGES, ins);
        assertEquals(m.popInt(), i);
    }

    @Test
    public void shouldCallAddIntFunction() {
        int a = 123, b = -123;
        Instruction[] funIns = new Instruction[] {
                new LocalGet(0),
                new LocalGet(1),
                IntBinaryInstruction.I32_ADD
        };
        Function fun = new Function(new DataType[]{DataType.I32, DataType.I32}, null, new DataType[]{DataType.I32}, funIns);
        Instruction[] ins = new Instruction[] {
                new IntConst(b),
                new IntConst(a),
                new Call(0),
                new IntConst(b),
                new IntConst(a),
                IntBinaryInstruction.I32_ADD,
                IntBinaryInstruction.I32_EQ
        };
        Machine m = Machine.createAndExecute(new Function[]{fun}, null, MEM_PAGES, ins);
        assertEquals(m.popInt(), 1);
    }

    @Test
    public void shouldLoop() {
        Variable[] globals = new Variable[]{Variable.newVariable(DataType.I32, 0)};
        Instruction[] loopIns = new Instruction[] {
                new GlobalGet(0),
                new IntConst(1),
                IntBinaryInstruction.I32_ADD,
                new GlobalSet(0),
                new GlobalGet(0),
                new IntConst(10),
                IntBinaryInstruction.I32_LT_S,
                new BranchIf(1),
        };
        Instruction[] ins = new Instruction[] {
                new Loop(1, loopIns),
                new GlobalGet(0),
        };
        Machine m = Machine.createAndExecute(null, globals, MEM_PAGES, ins);
        assertEquals(m.popInt(), 10);
    }
}
