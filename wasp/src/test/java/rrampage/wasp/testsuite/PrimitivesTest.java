package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;
import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class PrimitivesTest {
    @TestFactory  public Stream<DynamicTest> testI32() { return wastTest("i32.json");}
    @TestFactory  public Stream<DynamicTest> testI64() { return wastTest("i64.json");}
    @TestFactory public Stream<DynamicTest> testF32() {return wastTest("f32.json");}
    @TestFactory public Stream<DynamicTest> testF64() { return wastTest("f64.json");}
}
