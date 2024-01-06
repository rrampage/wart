package rrampage.wasp;
import org.junit.jupiter.api.Test;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.Module;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.data.Variable;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.instructions.ControlFlowInstruction;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.ImportUtils;

import java.util.Map;
import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.TestUtils.*;
public class WasmRunnerTest {
    private static final long[] EMPTY_STACK = new long[]{};

    private Module parseModule(String fileName) {
        return new WasmParser(readBinaryFile(getFilePath(fileName))).parseModule();
    }

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
    public void shouldRunFactorial() {
        var module = parseModule("./testsuite/fac.0.wasm");
        var machine = module.instantiate(null);
        machine.invoke("fac-iter", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
        machine.invoke("fac-iter-named", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
        machine.invoke("fac-rec", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
        machine.invoke("fac-rec-named", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
        machine.invoke("fac-opt", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
        machine.invoke("fac-ssa", ConstInstruction.of(new ConstInstruction.LongConst(25)));
        assertEquals(7034535277573963776L, machine.pop());
    }
}
