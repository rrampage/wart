package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class FunctionTest {
    @TestFactory public Stream<DynamicTest> testFunc() {return wastTest("func.json");}
    @TestFactory public Stream<DynamicTest> testCall() {return wastTest("call.json");}
    @TestFactory public Stream<DynamicTest> testCallIndirect() {return wastTest("call_indirect.json");}
    @TestFactory public Stream<DynamicTest> testUnwind() {return wastTest("unwind.json");}
    @TestFactory public Stream<DynamicTest> testReturn() {return wastTest("return.json");}
    @TestFactory public Stream<DynamicTest> testLocalGet() {return wastTest("local_get.json");}
    @TestFactory public Stream<DynamicTest> testLocalSet() {return wastTest("local_set.json");}
    @TestFactory public Stream<DynamicTest> testLocalTee() {return wastTest("local_tee.json");}
}
