package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.*;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.instructions.ConstInstruction;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class FactorialTest {
    private static final AssertReturn[] assertReturnTestCases = new AssertReturn[] {
            new AssertReturn("fac-iter", "1", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-iter-named", "1", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-rec", "1",ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-rec-named","1", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-opt","1", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-ssa","1", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
    };

    TestSuiteRunner runner = new TestSuiteRunner("./testsuite/fac.0.wasm");

    @Test()
    @Tag("high-resource")
    public void shouldRunFactorialAndThrowStackOverflowException() {
        assertThrows(StackOverflowError.class, () -> runner.getMachine().invoke("fac-rec", ConstInstruction.of(constOf(1073741824))));
    }

    @Test
    @Tag("high-resource")
    public void shouldComputeFactorial() {
        for (int a = 0; a < 10; a++) {
            var t = System.nanoTime();
            var n = 2000;
            for (int i = 0; i < n; i++) {
                runner.getMachine().invoke("fac-rec", ConstInstruction.of(constOf(40)));
            }
            System.out.println("Time: " + (System.nanoTime() - t)/n/1000);
        }
    }

    @TestFactory public Stream<DynamicTest> test() { return runner.test(assertReturnTestCases);}
    @TestFactory public Stream<DynamicTest> testStack() { return wastTest("stack.json");}
}
