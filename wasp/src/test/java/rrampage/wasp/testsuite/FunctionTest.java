package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class FunctionTest {
    @TestFactory public Stream<DynamicTest> testCall() {return wastTest("call.json");}
    @TestFactory public Stream<DynamicTest> testCallIndirect() {return wastTest("call_indirect.json");}
}
