package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class ControlFlowTest {
    @TestFactory public Stream<DynamicTest> testBlock() {return wastTest("block.json");}
    @TestFactory public Stream<DynamicTest> testLoop() {return wastTest("loop.json");}
}