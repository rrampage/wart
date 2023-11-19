package rrampage.waja.wasm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MachineTest {
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue( true );
    }

    @Test
    public void shouldPushConst() {
        Machine m = new Machine(65536);
        Instruction[] ins = new Instruction[]{
                new FloatConst(2.0f),
                new DoubleConst(1.0),
                new IntConst(4),
                new LongConst(1112345345667L),
        };
        m.execute(ins);
        assertEquals(1112345345667L, m.pop());
        assertEquals(4, m.popInt());
        assertEquals(1.0, m.popDouble(), 0.0);
        assertEquals(2.0f, m.popFloat(), 0.0f);
    }

    @Test
    public void shouldAdd() {
        Machine m = new Machine(65536);
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
        m.execute(ins);
        assertEquals(ll+lr, m.pop());
        assertEquals(il+ir, m.popInt());
        assertEquals(0.0f, m.popFloat(), 0.0f);
        assertEquals(m.popDouble(), l + r, 0.0);
    }

    @Test
    public void shouldAddAndCompare() {
        Machine m = new Machine(65536);
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
        m.execute(ins);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
    }
}
