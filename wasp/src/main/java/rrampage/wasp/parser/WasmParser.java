package rrampage.wasp.parser;

import rrampage.wasp.data.*;
import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.*;
import rrampage.wasp.parser.types.*;
import rrampage.wasp.utils.FileUtils;
import rrampage.wasp.utils.Leb128;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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
        ValueType[] params = new ValueType[numParams];
        for (int i = 0; i < numParams; i++) {
            params[i] = ValueType.from(bb.get());
        }
        int numReturns = (int) Leb128.readUnsigned(bb);
        ValueType[] returns = new ValueType[numReturns];
        for (int i = 0; i < numReturns; i++) {
            returns[i] = ValueType.from(bb.get());
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

    private Memory[] parseMemorySection(ImportMetadata[] imports) {
        int n = (int) Leb128.readUnsigned(bb);
        int numImports = (int) Arrays.stream(imports).filter(i -> i.importDescriptor() instanceof ImportDescriptor.MemoryDescriptor).count();
        Memory[] memories = new Memory[n + numImports];
        for (int i = numImports; i < n+numImports; i++) {
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

    private Table[] parseTableSection(ImportMetadata[] imports) {
        int n = (int) Leb128.readUnsigned(bb);
        int numImports = (int) Arrays.stream(imports).filter(i -> i.importDescriptor() instanceof ImportDescriptor.TableDescriptor).count();
        Table[] tables = new Table[n+numImports];
        for (int i = numImports; i < n+numImports; i++) {
            tables[i] = parseTable();
        }
        return tables;
    }

    private Variable parseGlobal() {
        var type = ValueType.from(bb.get());
        boolean isMutable = bb.get() == 0x1;
        var expr = parseConstantExpression();
        return Variable.newVariable(type, expr, isMutable);
    }

    private Variable[] parseGlobalSection(ImportMetadata[] imports) {
        int n = (int) Leb128.readUnsigned(bb);
        int numImports = (int) Arrays.stream(imports).filter(i -> i.importDescriptor() instanceof ImportDescriptor.GlobalDescriptor).count();
        Variable[] globals = new Variable[n+numImports];
        for (int i = numImports; i < n+numImports; i++) {
            globals[i] = parseGlobal();
        }
        return globals;
    }

    private ConstExpression parseConstantExpression() {
        // https://www.w3.org/TR/wasm-core-2/valid/instructions.html#valid-constant
        int b = Byte.toUnsignedInt(bb.get());
        // System.out.printf("Parsed Bytecode: 0x%x\n", b);
        ConstExpression ins = switch (b) {
            case ByteCodeConstants.CONST_INT -> new ConstInstruction.IntConst((int) Leb128.readSigned(bb));
            case ByteCodeConstants.REF_NULL -> new RefTypeInstruction.RefNull(ValueType.RefType.from(bb.get()));
            case ByteCodeConstants.REF_FUNC -> new RefTypeInstruction.RefFunc((int) Leb128.readUnsigned(bb));
            // TODO : Validation logic on type of global ??
            case ByteCodeConstants.GLOBAL_GET -> new GlobalInstruction.GlobalGet((int) Leb128.readUnsigned(bb));
            default -> throw new RuntimeException(String.format("PARSE_CONST_EXPR: Unexpected bytecode 0x%x", b));
        };
        // Ensure that we read "end" byte for expression body
        b = bb.get();
        assert b == 0xb;
        return ins;
    }

    private ElementSegment parseElementSegment() {
        // bit 0 = 0 active
        // bit 1 = if active and bit1, table uses index different from 0. if not active and bit1, declarative
        // bit 2 = 1 => expressions else index
        /*
            0x00 ex y*: vec(funcidx)
            0x01 et: elemtype y*
            0x02 x: tableIdx ex et y*
            0x03 et y*
            0x04 ex el*: vec(expr)
            0x05 et el*
            0x06 x ex refType el*
            0x07 et el*
         */
        byte eb = bb.get();
        boolean isActive = eb % 2 == 0;
        boolean isExpression = (eb >> 2) % 2 == 1;
        boolean isNonZeroIndex = isActive && (eb >> 1) % 2 == 1;
        boolean isDeclarative = !isActive && (eb >> 1) % 2 == 1;
        boolean isPassive = !isActive && !isDeclarative;
        System.out.printf("Element is active: %b passive: %b declarative: %b non-zero index: %b expression: %b\n",
                isActive, isPassive, isDeclarative, isNonZeroIndex, isExpression);
        int tableIndex = (isNonZeroIndex) ? (int) Leb128.readUnsigned(bb) : 0;
        ConstExpression expr = null;
        if (isActive) {
            expr = parseConstantExpression();
        }
        byte et = 0x0;
        if (!isActive || isNonZeroIndex) {
            et = bb.get();
        }
        ValueType.RefType segmentType = (et == 0x0) ? ValueType.RefType.FUNCREF : ValueType.RefType.from(et);
        // initialize either funcIdx or expr vector
        int n = (int) Leb128.readUnsigned(bb);
        int[] funcIdxVector = (isExpression) ? null : new int[n];
        ConstExpression[] expressionVector = (isExpression) ? new ConstExpression[n] : null;
        for (int i = 0; i < n; i++) {
            if (isExpression) {
                expressionVector[i] = parseConstantExpression();
            } else {
                funcIdxVector[i] = (int) Leb128.readSigned(bb);
            }
        }
        if (isActive) {
            return new ElementSegment.ActiveElementSegment(tableIndex, expr, segmentType, isExpression, funcIdxVector, expressionVector);
        }
        return (isDeclarative) ?
                new ElementSegment.DeclarativeElementSegment(segmentType, isExpression, funcIdxVector, expressionVector) :
                new ElementSegment.PassiveElementSegment(segmentType, isExpression, funcIdxVector, expressionVector);
    }

    private ElementSegment[] parseElementSection() {
        int n = (int) Leb128.readUnsigned(bb);
        System.out.println("Element table size: " + n);
        ElementSegment[] elementSegments = new ElementSegment[n];
        for (int i = 0; i < n; i++) {
            elementSegments[i] = parseElementSegment();
        }
        return elementSegments;
    }

    private Function parseFunctionCode(int funcIdx, FunctionType[] types, int numImports, int[] functions) {
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
        int funSize = (int) Leb128.readUnsigned(bb);
        int funPos = bb.position();
        int lc = (int) Leb128.readUnsigned(bb);
        ArrayList<ValueType> locals = new ArrayList<>();
        System.out.printf("Index: %d Function size: %d Local declaration count %d\n", funcIdx - numImports, funSize, lc);
        for (int j = 0; j < lc; j++) {
            int numLocal = (int) Leb128.readUnsigned(bb);
            var type = ValueType.from(bb.get());
            System.out.printf("%d Locals of type %s\n", numLocal, type);
            for (int k = 0; k < numLocal; k++) {
                locals.add(type);
            }
        }
        int bytesToParse = funPos + funSize - bb.position();
        Instruction[] code = InstructionParser.parse(bb, bytesToParse, types);
        System.out.println("Code : " + Arrays.toString(code));
        String fname = "Function_" + funcIdx;
        Function f = new Function(fname, types[functions[funcIdx - numImports]], locals.toArray(ValueType[]::new), code);
        byte endByte = bb.get();
        assert endByte == 0xb;
        assertBufferPosition(funPos + funSize);
        return f;
    }

    private Function[] parseCodeSection(FunctionType[] types, ImportMetadata[] imports, int[] functions) {
        int numImports = (int) Arrays.stream(imports).filter(i -> i.importDescriptor() instanceof ImportDescriptor.FunctionDescriptor).count();
        int n = (int) Leb128.readUnsigned(bb);
        System.out.printf("%d code items numFunctionsDeclared: %d numImports: %d\n", n, functions.length, numImports);
        Function[] allFuncs = new Function[numImports + functions.length];
        for (int i = 0; i < n; i++) {
            Function f = parseFunctionCode(i + numImports, types, numImports, functions);
            allFuncs[i + numImports] = f;
        }
        // create stub functions for import
        int i = 0;
        for (ImportMetadata im : imports) {
            if (im.importDescriptor() instanceof ImportDescriptor.FunctionDescriptor ifun) {
                i++;
                allFuncs[i] = Function.createStubFunction(im.module() + "__" + im.name(), types[ifun.idx()]);
            }
        }
        return allFuncs;
    }

    private Optional<Long> parseDataCountSection() {
        long numSegments = Leb128.readUnsigned(bb);
        if (numSegments > Integer.MAX_VALUE) {
            throw new RuntimeException("Invalid number of data segments");
        }
        return Optional.of(numSegments);
    }

    private DataSegment parseDataSegment() {
        byte b = bb.get();
        if (b % 2 == 0) {
            // Active segment
            int memIdx = 0;
            if (b == 2) {
                memIdx = (int) Leb128.readUnsigned(bb);
            }
            ConstExpression expr = parseConstantExpression();
            int size = (int) Leb128.readUnsigned(bb);
            byte[] data = new byte[size];
            bb.get(data);
            return new DataSegment.ActiveDataSegment(memIdx, expr, data);
        } else {
            // Passive segment
            int size = (int) Leb128.readUnsigned(bb);
            byte[] data = new byte[size];
            bb.get(data);
            return new DataSegment.PassiveDataSegment(data);
        }
    }

    private DataSegment[] parseDataSection() {
        int n = (int) Leb128.readUnsigned(bb);
        System.out.println("Num data sections: " + n);
        DataSegment[] dataSegments = new DataSegment[n];
        for (int i = 0; i < n; i++) {
            dataSegments[i] = parseDataSegment();
        }
        return dataSegments;
    }

    private void assertBufferPosition(int expectedPosition) {
        // System.out.printf("ASSERT Current: %d Expected: %d\n", bb.position(), expectedPosition);
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
        Function[] allFuncs = new Function[0];
        DataSegment[] dataSegments = new DataSegment[0];
        ElementSegment[] elementSegments = new ElementSegment[0];
        Optional<Long> dataCount = Optional.empty();
        Variable[] globals = new Variable[0];
        while (bb.hasRemaining()) {
            SectionType st = getSectionType(bb.get());
            int sectionLength = (int) Leb128.readUnsigned(bb);
            int sectionStart = bb.position();
            System.out.printf("Section: %s Length: %d\n", st, sectionLength);
            // Just skipping for now
            switch (st) {
                case CUSTOM -> bb.position(sectionStart + sectionLength);
                case TYPE -> types = parseTypes();
                case IMPORT -> imports = parseImports();
                case FUNCTION -> functions = parseFunctionSection();
                case TABLE -> tables = parseTableSection(imports);
                case GLOBAL -> globals = parseGlobalSection(imports);
                case EXPORT -> exports = parseExports();
                case START -> startIdx = parseStartIdx();
                case MEMORY -> memories = parseMemorySection(imports);
                case ELEMENT -> elementSegments = parseElementSection();
                case DATA_COUNT -> dataCount = parseDataCountSection(); // Must be parsed before code to validate mem.init and mem.drop instructions using passive segments
                case CODE -> allFuncs = parseCodeSection(types, imports, functions);
                case DATA -> dataSegments = parseDataSection();
            }
            // Check that section is fully consumed
            assertBufferPosition(sectionStart + sectionLength);
        }
        System.out.println(bb.position());
        System.out.println(Arrays.toString(types));
        System.out.println(Arrays.toString(imports));
        System.out.println(Arrays.toString(exports));
        System.out.println(Arrays.toString(memories));
        System.out.println(Arrays.toString(tables));
        System.out.println("Start Index: " + startIdx);
        System.out.println("Data count: " + dataCount);
        assert dataCount.isEmpty() || dataSegments.length == dataCount.get();
        return new Module(1, types, allFuncs, tables, exports, imports, memories, dataSegments, elementSegments, globals, startIdx);
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
        String f7 = "./examples/waforth.wasm";
        String f8 = "./examples/ruffle.wasm"; // Needs v128 support
        String f9 = "./examples/ruby.wasm";
        String f10 = "./examples/sqlite3.wasm";
        String path = Paths.get(f2).toAbsolutePath().normalize().toString();
        System.out.println("Path: " + path);
        byte[] data = FileUtils.readBinaryFile(path);
        System.out.println("Data read: " + data.length);
        System.out.println("B0: " + bytesToHex(Arrays.copyOf(data, 16)));
        WasmParser wp = fromFile(path);
        wp.parseModule();
    }
}
