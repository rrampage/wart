package rrampage.wasp.testsuite;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.vm.MachineVisitors;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class ControlFlowTest {
    TestSuiteRunner runner = new TestSuiteRunner("./testsuite/block.0.wasm", null, MachineVisitors.logVisitor());
    @TestFactory public Stream<DynamicTest> testBlock() {
        // TODO: Fix breaking
        return wastTest("block.0.wasm", "block.json");
    }
    @TestFactory public Stream<DynamicTest> testLoop() {
        // TODO: Fix breaking
        return wastTest("loop.0.wasm", "loop.json").limit(10);
    }
}