package rrampage.wasp;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.data.Module;
import rrampage.wasp.parser.WasmParser;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.TestUtils.*;
public class WasmParserTest {
    private static final WasmParserTestCase[] testCases = new WasmParserTestCase[] {
            new WasmParserTestCase("empty_module.wasm", 8, 0, 0,0),
            new WasmParserTestCase("add_two.wasm", 82, 3, 0, 0),
            new WasmParserTestCase("fizzbuzz_manual.wasm", 326, 4, 0, 0),
    };

    record WasmParserTestCase(String fileName, int numBytes, int numTypes, int numImports, int numFunctions) {
        public void check() {
            byte[] data = readBinaryFile(getFilePath(fileName));
            WasmParser parser = new WasmParser(data);
            Module module = parser.parseModule();
            var parsedNumTypes = module == null || module.types() == null ? 0 : module.types().length;
            assertEquals(numBytes, data.length, String.format("test bytes read for %s", fileName));
            assertEquals(numTypes, parsedNumTypes, String.format("test types for %s", fileName));
        }
    }

    @TestFactory
    public Stream<DynamicTest> testWasmParser() {
        return DynamicTest.stream(Stream.of(testCases), WasmParserTestCase::fileName, WasmParserTestCase::check);
    }

}
