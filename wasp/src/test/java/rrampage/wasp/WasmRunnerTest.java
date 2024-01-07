package rrampage.wasp;

import org.junit.jupiter.api.*;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.utils.ImportUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.TestUtils.*;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class WasmRunnerTest {
    private static final long[] EMPTY_STACK = new long[]{};

    @Test
    public void shouldRunEmptyModule() {
        var module = parseModule("./empty_module.wasm");
        var machine = module.instantiate(null);
        assertArrayEquals(machine.inspectStack(), EMPTY_STACK);
    }
    @Test
    public void shouldRunAddModule() {
        var module = parseModule("./add_two.wasm");
        try {
            var machine = module.instantiate(Map.of("host", Map.of("print", ImportUtils.generateLoggerHandle(new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32})))));
            assertArrayEquals(machine.inspectStack(), EMPTY_STACK);
            // Run "start" again and assert that stack is empty
            machine.start();
            assertArrayEquals(machine.inspectStack(), EMPTY_STACK);
        } catch (Exception e) {
            fail("Got exception: " + e.getMessage());
        }
    }

    @Test
    public void shouldInstantiateCallIndirect() {
        var module = parseModule("./call_indirect_example.wasm");
        try {
            var machine = module.instantiate(Map.of("env", Map.of("jstimes3",
                    ImportUtils.generateLoggerHandle(new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32})))));
            assertEquals(2, machine.exports().size());
        } catch (Exception e) {
            fail("Got exception: " + e.getMessage());
        }
    }

    @Test
    public void shouldHandleMultipleReturn() {
        var module = parseModule("./reduced_loop.wasm");
        var machine = module.instantiate(null);
        assertTrue(invokeAndCheckStack(machine, "break-br_if-num-num", ConstInstruction.of(constOf(0)), ConstInstruction.of(constOf(51),constOf(52))));
        assertTrue(invokeAndCheckStack(machine, "break-br_if-num-num", ConstInstruction.of(constOf(1)), ConstInstruction.of(constOf(50),constOf(51))));
    }
}
