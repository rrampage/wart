package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class ControlFlowTest {
    @TestFactory public Stream<DynamicTest> testBlock() {
        // TODO: Fix breaking
        return wastTest("block.0.wasm", "block.json").limit(10);
    }
    @TestFactory public Stream<DynamicTest> testLoop() {
        // TODO: Fix breaking
        return wastTest("loop.0.wasm", "loop.json").limit(10);
    }
}