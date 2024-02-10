package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class MemoryTest {
    @TestFactory public Stream<DynamicTest> testMemory() {return wastTest("memory.json");}
    @TestFactory public Stream<DynamicTest> testMemoryInit() {return wastTest("memory_init.json");}
    @TestFactory public Stream<DynamicTest> testMemorySize() {return wastTest("memory_size.json");}
    @TestFactory public Stream<DynamicTest> testMemoryRedundancy() {return wastTest("memory_redundancy.json");}
    @TestFactory public Stream<DynamicTest> testEndianness() {return wastTest("endianness.json");}
    @TestFactory public Stream<DynamicTest> testAddress() {return wastTest("address.json");}
    @TestFactory public Stream<DynamicTest> testAlign() {return wastTest("align.json");}
}
