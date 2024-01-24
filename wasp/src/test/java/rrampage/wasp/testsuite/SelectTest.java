package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static rrampage.wasp.TestUtils.invokeAndCheckStack;
import static rrampage.wasp.TestUtils.parseModule;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class SelectTest {
    private static final AssertReturn[] assertReturnTestCases = new AssertReturn[] {
            new AssertReturn("select-i32", ConstInstruction.of(constOf(1), constOf(2), constOf(1)), ConstInstruction.of(constOf(1))),
            new AssertReturn("select-i64", ConstInstruction.of(constOf(2L), constOf(1L), constOf(1)), ConstInstruction.of(constOf(2L))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(1065353216f), constOf(1073741824f), constOf(1)), ConstInstruction.of(constOf(1065353216f))),
            new AssertReturn("select-f64", ConstInstruction.of(constOf(4607182418800017408.0), constOf(4611686018427387904.0), constOf(1)), ConstInstruction.of(constOf(4607182418800017408.0))),
            new AssertReturn("select-i32", ConstInstruction.of(constOf(1), constOf(2), constOf(0)), ConstInstruction.of(constOf(2))),
            new AssertReturn("select-i32", ConstInstruction.of(constOf(2), constOf(1), constOf(0)), ConstInstruction.of(constOf(1))),
            new AssertReturn("select-i64", ConstInstruction.of(constOf(2L), constOf(1L), constOf(Integer.parseUnsignedInt("4294967294"))), ConstInstruction.of(constOf(2L))),
            new AssertReturn("select-i64", ConstInstruction.of(constOf(2L), constOf(1L), constOf(Integer.parseUnsignedInt("4042322160"))), ConstInstruction.of(constOf(2L))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(2143289344f), constOf(1065353216f), constOf(1)), ConstInstruction.of(constOf(2143289344f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(2139226884f), constOf(1065353216f), constOf(1)), ConstInstruction.of(constOf(2139226884f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(2143289344f), constOf(1065353216f), constOf(0)), ConstInstruction.of(constOf(1065353216f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(2139226884f), constOf(1065353216f), constOf(0)), ConstInstruction.of(constOf(1065353216f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(1073741824f), constOf(2143289344f), constOf(1)), ConstInstruction.of(constOf(1073741824f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(1073741824f), constOf(2139226884f), constOf(1)), ConstInstruction.of(constOf(1073741824f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(1073741824f), constOf(2143289344f), constOf(0)), ConstInstruction.of(constOf(2143289344f))),
            new AssertReturn("select-f32", ConstInstruction.of(constOf(1073741824f), constOf(2139226884f), constOf(0)), ConstInstruction.of(constOf(2139226884f))),
            new AssertReturn("select-i32-t", ConstInstruction.of(constOf(1), constOf(2), constOf(1)), ConstInstruction.of(constOf(1))),
            new AssertReturn("select-i64-t", ConstInstruction.of(constOf(2L), constOf(1L), constOf(1)), ConstInstruction.of(constOf(2L))),
    };

    Module module = parseModule("./testsuite/select.0.wasm");
    Machine machine = module.instantiate(null, MachineVisitors.logVisitor());

    public void check(AssertReturn test) {
        assertTrue(invokeAndCheckStack(machine, test.function(), test.args(), test.expected()));
    }

    @TestFactory
    public Stream<DynamicTest> test() {
        return DynamicTest.stream(Stream.of(assertReturnTestCases), AssertReturn::toString, this::check);
    }
}
