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
            new WasmParserTestCase("empty_module.wasm", 8, 0, 0,0, 0),
            new WasmParserTestCase("add_two.wasm", 82, 3, 1, 0, 1),
            new WasmParserTestCase("fizzbuzz_manual.wasm", 326, 4, 1, 0, 0),
            new WasmParserTestCase("import_global.wasm", 104, 2, 5, 0, 0),
            new WasmParserTestCase("walloc.wasm", 2023, 3, 1, 0, 2),
    };

    record WasmParserTestCase(String fileName, int numBytes, int numTypes, int numImports, int numFunctions, int numExports) {
        public void check() {
            byte[] data = readBinaryFile(getFilePath(fileName));
            WasmParser parser = new WasmParser(data);
            Module module = parser.parseModule();
            var parsedNumTypes = module == null || module.types() == null ? 0 : module.types().length;
            var parsedNumImports = module == null || module.imports() == null ? 0 : module.imports().length;
            var parsedNumExports = module == null || module.exports() == null ? 0 : module.exports().length;
            assertEquals(numBytes, data.length, String.format("test bytes read for %s", fileName));
            assertEquals(numTypes, parsedNumTypes, String.format("test types for %s", fileName));
            assertEquals(numImports, parsedNumImports, String.format("test imports for %s", fileName));
            assertEquals(numExports, parsedNumExports, String.format("test exports for %s", fileName));
        }
    }

    @TestFactory
    public Stream<DynamicTest> testWasmParser() {
        return DynamicTest.stream(Stream.of(testCases), WasmParserTestCase::fileName, WasmParserTestCase::check);
    }

}
