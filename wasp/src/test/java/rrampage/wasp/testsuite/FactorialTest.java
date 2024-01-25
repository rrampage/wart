package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.*;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.instructions.ConstInstruction;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class FactorialTest {
    private static final AssertReturn[] assertReturnTestCases = new AssertReturn[] {
            new AssertReturn("fac-iter", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-iter-named", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-rec", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-rec-named", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-opt", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
            new AssertReturn("fac-ssa", ConstInstruction.of(constOf(25L)), ConstInstruction.of(constOf(7034535277573963776L))),
    };

    TestSuiteRunner runner = new TestSuiteRunner("./testsuite/fac.0.wasm");

    @Test()
    @Tag("high-resource")
    public void shouldRunFactorialAndThrowStackOverflowException() {
        assertThrows(StackOverflowError.class, () -> runner.getMachine().invoke("fac-rec", ConstInstruction.of(constOf(1073741824))));
    }

    @TestFactory
    public Stream<DynamicTest> test() {
        return runner.test(assertReturnTestCases);
    }
}
