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
        Instruction[] ins = new Instruction[]{
                new DoubleConst(l),
                new DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new FloatConst(fl),
                new FloatConst(fr),
                FloatBinaryInstruction.F32_ADD,
        };
        m.execute(ins);
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
        };
        m.execute(ins);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
    }
}
