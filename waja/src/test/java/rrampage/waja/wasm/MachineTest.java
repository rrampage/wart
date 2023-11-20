package rrampage.waja.wasm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MachineTest {
    private static final int MEM_SIZE = 65536;

    @Test
    public void shouldPushConst() {
        Instruction[] ins = new Instruction[]{
                new FloatConst(2.0f),
                new DoubleConst(1.0),
                new IntConst(4),
                new LongConst(1112345345667L),
        };
        Machine m = Machine.createAndExecute(MEM_SIZE, ins);
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
        Machine m = Machine.createAndExecute(MEM_SIZE, ins);
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
        Machine m = Machine.createAndExecute(MEM_SIZE, ins);
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
        Machine m = Machine.createAndExecute(MEM_SIZE, ins);
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
        int i = 1111;
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
                StoreInstruction.I32_STORE,
                StoreInstruction.F32_STORE,
                StoreInstruction.I64_STORE,
                StoreInstruction.F64_STORE,
                new IntConst(dAddr),
                LoadInstruction.F64_LOAD,
                new IntConst(lAddr),
                LoadInstruction.I64_LOAD,
                new IntConst(fAddr),
                LoadInstruction.F32_LOAD,
                new IntConst(iAddr),
                LoadInstruction.I32_LOAD,
        };
        Machine m = Machine.createAndExecute(MEM_SIZE, ins);
        assertEquals(m.popInt(), i);
        assertEquals(m.popFloat(), f, 0.0f);
        assertEquals(m.pop(), l);
        assertEquals(m.popDouble(), d, 0.0);
    }
}
