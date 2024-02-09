package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class PrimitivesTest {
    @TestFactory public Stream<DynamicTest> testI32() { return wastTest("i32.json");}
    @TestFactory public Stream<DynamicTest> testI64() { return wastTest("i64.json");}
    @TestFactory public Stream<DynamicTest> testF32() {return wastTest("f32.json");}
    @TestFactory public Stream<DynamicTest> testF64() { return wastTest("f64.json");}
    @TestFactory public Stream<DynamicTest> testConversions() { return wastTest("conversions.json");}
    @TestFactory public Stream<DynamicTest> testIntLiterals() { return wastTest("int_literals.json");}
    @TestFactory public Stream<DynamicTest> testIntExpressions() { return wastTest("int_exprs.json");}
    @TestFactory public Stream<DynamicTest> testF32Bitwise() {return wastTest("f32_bitwise.json");}
    @TestFactory public Stream<DynamicTest> testF64Bitwise() { return wastTest("f64_bitwise.json");}
    @TestFactory public Stream<DynamicTest> testF32Cmp() {return wastTest("f32_cmp.json");}
    @TestFactory public Stream<DynamicTest> testF64Cmp() { return wastTest("f64_cmp.json");}
    @TestFactory public Stream<DynamicTest> testFloatLiterals() {return wastTest("float_literals.json");}
    @TestFactory public Stream<DynamicTest> testFloatExpressions() { return wastTest("float_exprs.json");}
    @TestFactory public Stream<DynamicTest> testFloatMisc() {return wastTest("float_misc.json");}
}
