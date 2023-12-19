package rrampage.wasp.parser;

import rrampage.wasp.data.*;
import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.Instruction;
import rrampage.wasp.parser.types.*;
import rrampage.wasp.utils.FileUtils;
import rrampage.wasp.utils.Leb128;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;

import static rrampage.wasp.utils.ConversionUtils.*;

public class WasmParser implements Parser {
    private static final int MAGIC_HEADER = bytesToInt(new byte[]{0, 'a', 's', 'm'}); // 1836278016
    private static final int MIN_VERSION = 1;
    private static final int MAX_VERSION = 1;

    private static final byte FUNCTYPE_TAG = 0x60;

    private final byte[] data;
    private ByteBuffer bb;
    public WasmParser(byte[] data) {
        this.data = data; // Keep the reference to the data in case we need to recreate byte buffer later
        this.bb = bytesToBuffer(data);
    }

    private boolean checkMagicBytes(int val) {
        return val == MAGIC_HEADER;
    }

    private boolean checkVersion(int val) {
        return val >= MIN_VERSION && val <= MAX_VERSION;
    }

    private SectionType getSectionType(byte val) {
        return SectionType.values()[val];
    }

    private FunctionType parseFunctionType() {
        byte fb = bb.get();
        if (fb != FUNCTYPE_TAG) {
            System.out.println("Invalid type");
            return null;
        }
        int numParams = (int) Leb128.readUnsigned(bb);
        ValueType.NumType[] params = new ValueType.NumType[numParams];
        for (int i = 0; i < numParams; i++) {
            params[i] = ValueType.NumType.from(bb.get());
        }
        int numReturns = (int) Leb128.readUnsigned(bb);
        ValueType.NumType[] returns = new ValueType.NumType[numReturns];
        for (int i = 0; i < numReturns; i++) {
            returns[i] = ValueType.NumType.from(bb.get());
        }
        return new FunctionType(params, returns);
    }

    private FunctionType[] parseTypes() {
        int numTypes = (int) Leb128.readUnsigned(bb);
        FunctionType[] types = new FunctionType[numTypes];
        for (int i = 0; i < numTypes; i++) {
            types[i] = parseFunctionType();
        }
        return types;
    }

    private ExportDescriptor parseExportDescriptor() {
        byte ib = bb.get();
        if (ib > 3) {
            throw new RuntimeException("Invalid Descriptor type: " + ib);
        }
        int idx = (int) Leb128.readUnsigned(bb);
        ExportDescriptor desc = switch (ib) {
            case 0 -> new ExportDescriptor.FunctionDescriptor(idx);
            case 1 -> new ExportDescriptor.TableDescriptor(idx);
            case 2 -> new ExportDescriptor.MemoryDescriptor(idx);
            case 3 -> new ExportDescriptor.GlobalDescriptor(idx);
            default -> throw new RuntimeException("Invalid Descriptor type: " + ib);
        };
        return desc;
    }

    private ImportDescriptor parseImportDescriptor() {
        byte ib = bb.get();
        if (ib > 3) {
            System.out.println("Invalid Descriptor type: " + ib);
            return null;
        }
        ImportDescriptor desc = null;
        switch (ib) {
            case 0 -> {
                // Function
                int typeIdx = (int) Leb128.readUnsigned(bb);
                desc = new ImportDescriptor.FunctionDescriptor(typeIdx);
            }
            case 1 -> {
                // Table
                var refType = ValueType.RefType.from(bb.get());
                byte fb = bb.get();
                int min = (int) Leb128.readUnsigned(bb);
                int max = (fb == 1) ? (int) Leb128.readUnsigned(bb) : min;
                desc = new ImportDescriptor.TableDescriptor(refType, min, max);
            }
            case 2 -> {
                // Memory
                byte fb = bb.get();
                int min = (int) Leb128.readUnsigned(bb);
                int max = (fb == 1) ? (int) Leb128.readUnsigned(bb) : Memory.MAX_PAGES;
                desc = new ImportDescriptor.MemoryDescriptor(min, max);
            }
            case 3 -> {
                // Global
                var valType = ValueType.from(bb.get());
                boolean mutable = bb.get() == 1;
                desc = new ImportDescriptor.GlobalDescriptor(valType, mutable);
            }
        }
        return desc;
    }

    private ImportMetadata parseImport() {
        int ml = (int) Leb128.readUnsigned(bb);
        byte[] moduleName = new byte[ml];
        bb.get(moduleName);
        int fl = (int) Leb128.readUnsigned(bb);
        byte[] name = new byte[fl];
        bb.get(name);
        ImportDescriptor desc = parseImportDescriptor();
        return new ImportMetadata(new String(moduleName, StandardCharsets.UTF_8), new String(name, StandardCharsets.UTF_8), desc);
    }

    private ImportMetadata[] parseImports() {
        int numImports = (int) Leb128.readUnsigned(bb);
        ImportMetadata[] imports = new ImportMetadata[numImports];
        for (int i = 0; i < numImports; i++) {
            imports[i] = parseImport();
        }
        return imports;
    }

    private ExportMetadata parseExport() {
        int fl = (int) Leb128.readUnsigned(bb);
        byte[] name = new byte[fl];
        bb.get(name);
        String exportName = new String(name, StandardCharsets.UTF_8);
        System.out.printf("Export name: %s size: %d\n", exportName, fl);
        ExportDescriptor desc = parseExportDescriptor();
        return new ExportMetadata(exportName, desc);
    }

    private ExportMetadata[] parseExports() {
        int n = (int) Leb128.readUnsigned(bb);
        ExportMetadata[] exports = new ExportMetadata[n];
        for (int i = 0; i < n; i++) {
            exports[i] = parseExport();
        }
        return exports;
    }

    private int[] parseFunctionSection() {
        int numFunctions = (int) Leb128.readUnsigned(bb);
        int[] functions = new int[numFunctions];
        for (int i = 0; i < numFunctions; i++) {
            functions[i] = (int) Leb128.readUnsigned(bb);
        }
        return functions;
    }

    private long parseStartIdx() {
        var startIdx = Leb128.readUnsigned(bb);
        if (startIdx < 0 || startIdx > 2L*Integer.MAX_VALUE-1) {
            startIdx = -1;
        }
        return startIdx;
    }

    private Memory parseMemory() {
        byte fb = bb.get();
        int min = (int) Leb128.readUnsigned(bb);
        if (fb == 0) {
            return new Memory(min);
        }
        int max = (int) Leb128.readUnsigned(bb);
        return new Memory(min, max);
    }

    private Memory[] parseMemorySection() {
        int numMem = (int) Leb128.readUnsigned(bb);
        Memory[] memories = new Memory[numMem];
        for (int i = 0; i < numMem; i++) {
            memories[i] = parseMemory();
        }
        return memories;
    }

    private Table parseTable() {
        var refType = ValueType.RefType.from(bb.get());
        byte fb = bb.get();
        int min = (int) Leb128.readUnsigned(bb);
        int max = (fb == 1) ? (int) Leb128.readUnsigned(bb) : min;
        return new Table(min, max, refType);
    }

    private Table[] parseTableSection() {
        int n = (int) Leb128.readUnsigned(bb);
        Table[] tables = new Table[n];
        for (int i = 0; i < n; i++) {
            tables[i] = parseTable();
        }
        return tables;
    }

    public void parseElementSection() {
        int n = (int) Leb128.readUnsigned(bb);
        System.out.println("Element table size: " + n);
        // bit 0 = 0 active
        // bit 1 = if active and bit1, table uses index different from 0. if not active and bit1, declarative
        // bit 2 = 1 => expressions else index
        for (int i = 0; i < n; i++) {
            byte eb = bb.get();
            boolean isActive = eb % 2 == 0;
            boolean isExpression = (eb >> 2) % 2 == 1;
            boolean isNonZeroIndex = isActive && (eb >> 1) % 2 == 1;
            boolean isDeclarative = !isActive && (eb >> 1) % 2 == 1;
            boolean isPassive = !isActive && !isDeclarative;
            System.out.printf("Element is active: %b passive: %b declarative: %b non-zero index: %b expression: %b\n",
                    isActive, isPassive, isDeclarative, isNonZeroIndex, isExpression);
            // TODO
        }
    }

    public void parseCodeSection() {
        int n = (int) Leb128.readUnsigned(bb);
        System.out.printf("%d code items\n", n);
        /*
            The encoding of each code entry consists of :
            - the size of the function code in bytes,
            - the actual function code, which in turn consists of:
              - the declaration of locals,
              - the function body as an expression.
            Local declarations are compressed into a vector whose entries consist of:
            - a count,
            - a value type
            denoting count locals of the same value type.

         */
        for (int i = 0; i < n; i++) {
            int funSize = (int) Leb128.readUnsigned(bb);
            int funPos = bb.position();
            int lc = (int) Leb128.readUnsigned(bb);
            System.out.printf("Index: %d Function size: %d Local declaration count %d\n", i, funSize, lc);
            for (int j = 0; j < lc; j++) {
                int numLocal = (int) Leb128.readUnsigned(bb);
                var type = ValueType.NumType.from(bb.get());
                System.out.printf("%d Locals of type %s\n", numLocal, type);
            }
            int bytesToParse = funPos + funSize - bb.position();
            Instruction[] code = InstructionParser.parse(bb, bytesToParse);
            System.out.println("Code : " + Arrays.toString(code));
            bb.position(funPos + funSize);
        }
    }

    private void assertBufferPosition(int expectedPosition) {
        assert bb.position() == expectedPosition;
    }

    public Module parseModule() {
        bb.rewind();
        // Check magic bytes
        // Parse sections
        int magic = bb.getInt();
        if (!checkMagicBytes(magic)) {
            System.out.println("INVALID_MAGIC_BYTES: " + magic);
            return null;
        }
        // Check version
        int version = bb.getInt();
        if (!checkVersion(version)) {
            System.out.println("INVALID_VERSION: " + version);
            return null;
        }
        FunctionType[] types = new FunctionType[0];
        ImportMetadata[] imports = new ImportMetadata[0];
        ExportMetadata[] exports = new ExportMetadata[0];
        Memory[] memories = new Memory[0];
        Table[] tables = new Table[0];
        long startIdx = -1;
        int[] functions = new int[0];
        while (bb.hasRemaining()) {
            SectionType st = getSectionType(bb.get());
            int sectionLength = (int) Leb128.readUnsigned(bb);
            int sectionStart = bb.position();
            System.out.printf("Section: %s Length: %d\n", st, sectionLength);
            // Just skipping for now
            switch (st) {
                case CUSTOM, GLOBAL, DATA, DATA_COUNT -> bb.position(sectionStart + sectionLength);
                case TYPE -> types = parseTypes();
                case IMPORT -> imports = parseImports();
                case FUNCTION -> functions = parseFunctionSection();
                case TABLE -> tables = parseTableSection();
                case EXPORT -> exports = parseExports();
                case START -> startIdx = parseStartIdx();
                case MEMORY -> memories = parseMemorySection();
                case ELEMENT -> {
                    // parseElementSection(sectionLength);
                    bb.position(sectionStart + sectionLength);
                }
                case CODE -> {
                    parseCodeSection();
                    bb.position(sectionStart + sectionLength);
                }
            }
            // Check that section is fully consumed
            assertBufferPosition(sectionStart + sectionLength);
        }
        System.out.println(bb.position());
        System.out.println(Arrays.toString(types));
        System.out.println(Arrays.toString(imports));
        System.out.println(Arrays.toString(exports));
        System.out.println(Arrays.toString(functions));
        System.out.println(Arrays.toString(memories));
        System.out.println(Arrays.toString(tables));
        System.out.println("Start Index: " + startIdx);
        return new Module(1, types, null, tables, exports, imports, memories, startIdx);
    }

    public static WasmParser fromFile(String path) throws IOException {
        byte[] source = FileUtils.readBinaryFile(path);
        return new WasmParser(source);
    }

    public static void main(String[] args) throws Exception {
        String f1 = "./examples/empty_module.wasm";
        String f2 = "./examples/import_global.wasm";
        String f3 = "./examples/add_two.wasm";
        String f4 = "./examples/fizzbuzz_manual.wasm";
        String f5 = "./examples/rocket.wasm";
        String f6 = "./examples/elem_syntax.wasm";
        String path = Paths.get(f6).toAbsolutePath().normalize().toString();
        System.out.println("Path: " + path);
        byte[] data = FileUtils.readBinaryFile(path);
        System.out.println("Data read: " + data.length);
        System.out.println("B0: " + bytesToHex(Arrays.copyOf(data, 16)));
        WasmParser wp = fromFile(path);
        wp.parseModule();
    }
}
