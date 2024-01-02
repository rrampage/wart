package rrampage.wasp;
import org.junit.jupiter.api.Test;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.Module;
import rrampage.wasp.data.ValueType;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.ImportUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.TestUtils.*;
public class WasmRunnerTest {
    private static final long[] EMPTY_STACK = new long[]{};
    @Test
    public void shouldRunAddModule() {
        String fileName = "./add_two.wasm";
        byte[] data = readBinaryFile(getFilePath(fileName));
        WasmParser parser = new WasmParser(data);
        Module module = parser.parseModule();
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
}
