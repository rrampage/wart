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
            new WasmParserTestCase("empty_module.wasm", 8, 0, 0,0, 0, 0, 0),
            new WasmParserTestCase("add_two.wasm", 82, 3, 1, 3, 1, 0, 0),
            new WasmParserTestCase("fizzbuzz_manual.wasm", 326, 4, 1, 6, 0, 2, 0),
            new WasmParserTestCase("import_global.wasm", 104, 2, 5, 0, 0, 0, 0),
            new WasmParserTestCase("walloc.wasm", 2023, 3, 1, 6, 2, 2, 0),
            new WasmParserTestCase("rocket.wasm", 53067, 17, 9, 125, 8, 198, 1),
            new WasmParserTestCase("data_segment_example.wasm", 88, 1, 1, 1, 0, 2, 0),
            new WasmParserTestCase("elem_syntax.wasm", 507, 2, 1, 2, 0, 0, 63),
    };

    record WasmParserTestCase(String fileName, int numBytes, int numTypes, int numImports, int numFunctions, int numExports, int numDataSegments, int numElementSegments) {
        public void check() {
            byte[] data = readBinaryFile(getFilePath(fileName));
            WasmParser parser = new WasmParser(data);
            Module module = parser.parseModule();
            var parsedNumTypes = module == null || module.types() == null ? 0 : module.types().length;
            var parsedNumImports = module == null || module.imports() == null ? 0 : module.imports().length;
            var parsedNumExports = module == null || module.exports() == null ? 0 : module.exports().length;
            var parsedNumFunctions = module == null || module.functions() == null ? 0 : module.functions().length;
            var parsedNumDataSegments = module == null || module.dataSegments() == null ? 0 : module.dataSegments().length;
            var parsedNumElementSegments = module == null || module.elementSegments() == null ? 0 : module.elementSegments().length;
            assertEquals(numBytes, data.length, String.format("test bytes read for %s", fileName));
            assertEquals(numTypes, parsedNumTypes, String.format("test types for %s", fileName));
            assertEquals(numImports, parsedNumImports, String.format("test imports for %s", fileName));
            assertEquals(numExports, parsedNumExports, String.format("test exports for %s", fileName));
            assertEquals(numFunctions, parsedNumFunctions, String.format("test functions for %s", fileName));
            assertEquals(numDataSegments, parsedNumDataSegments, String.format("test data segments for %s", fileName));
            assertEquals(numElementSegments, parsedNumElementSegments, String.format("test element segments for %s", fileName));
        }
    }

    @TestFactory
    public Stream<DynamicTest> testWasmParser() {
        return DynamicTest.stream(Stream.of(testCases), WasmParserTestCase::fileName, WasmParserTestCase::check);
    }

}
