package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.vm.MachineVisitors;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class ControlFlowTest {
    @TestFactory public Stream<DynamicTest> testBlock() {return wastTest("block.json");}
    @TestFactory public Stream<DynamicTest> testLoop() {return wastTest("loop.json");}
    @TestFactory public Stream<DynamicTest> testIf() {return wastTest("if.json");}
    @TestFactory public Stream<DynamicTest> testBr() {return wastTest("br.json");}
    @TestFactory public Stream<DynamicTest> testBrIf() {return wastTest("br_if.json");}
    public Stream<DynamicTest> testBrTable() {return wastTest("br_table.json");}
    @TestFactory public Stream<DynamicTest> testLabels() {return wastTest("labels.json");}

    @Test
    public void shouldRun() {
        TestSuiteRunner runner = new TestSuiteRunner("./testsuite/br.0.wasm", null, MachineVisitors.logVisitor());
        runner.getMachine().invoke("as-return-values");
    }
}