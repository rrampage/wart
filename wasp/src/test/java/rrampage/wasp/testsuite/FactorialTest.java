package rrampage.wasp.testsuite;

import org.junit.jupiter.api.*;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.*;
import rrampage.wasp.vm.MachineVisitors;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.TestUtils.*;
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
    Module module = parseModule("./testsuite/fac.0.wasm");
    Machine machine = module.instantiate(null, MachineVisitors.logVisitor());

    public void check(AssertReturn test) {
        assertTrue(invokeAndCheckStack(machine, test.function(), test.args(), test.expected()));
    }

    @Test()
    @Tag("high-resource")
    public void shouldRunFactorialAndThrowStackOverflowException() {
        assertThrows(StackOverflowError.class, () -> machine.invoke("fac-rec", ConstInstruction.of(constOf(1073741824))));
    }

    @TestFactory
    public Stream<DynamicTest> test() {
        return DynamicTest.stream(Stream.of(assertReturnTestCases), AssertReturn::function, this::check);
    }
}
