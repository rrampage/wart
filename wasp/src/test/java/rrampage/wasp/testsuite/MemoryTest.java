package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class MemoryTest {
    @TestFactory public Stream<DynamicTest> testMemory() {return wastTest("memory.json");}
}
