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
                new DoubleConst(1.0)
        };
        m.execute(ins);
        assertEquals(1.0, m.popDouble(), 0.0);
    }

    @Test
    public void shouldAdd() {
        Machine m = new Machine(65536);
        double l = 123.0;
        double r = 345.0;
        Instruction[] ins = new Instruction[]{
                new DoubleConst(l),
                new DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
        };
        m.execute(ins);
        assertEquals(m.popDouble(), l + r, 0.0);
    }

    @Test
    public void shouldAddAndCompare() {
        Machine m = new Machine(65536);
        double l = 123.0;
        double r = 345.0;
        double a = l+r;
        Instruction[] ins = new Instruction[]{
                new DoubleConst(l),
                new DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new DoubleConst(a),
                DoubleBinaryInstruction.F64_EQ
        };
        m.execute(ins);
        assertEquals(m.popInt(), 1);
    }
}
