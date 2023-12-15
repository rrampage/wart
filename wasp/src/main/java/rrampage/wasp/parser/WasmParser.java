package rrampage.wasp.parser;

import rrampage.wasp.data.*;
import rrampage.wasp.data.Module;
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
        DataType[] params = new DataType[numParams];
        for (int i = 0; i < numParams; i++) {
            params[i] = ValueType.fromValueType(ValueType.of(bb.get())) ;
        }
        int numReturns = (int) Leb128.readUnsigned(bb);
        DataType[] returns = new DataType[numReturns];
        for (int i = 0; i < numReturns; i++) {
            returns[i] = ValueType.fromValueType(ValueType.of(bb.get())) ;
        }
        return new FunctionType(params, returns);
    }

    private FunctionType[] parseTypes(int numBytes) {
        if (numBytes <= 0) {
            return null;
        }
        int numTypes = (int) Leb128.readUnsigned(bb);
        FunctionType[] types = new FunctionType[numTypes];
        for (int i = 0; i < numTypes; i++) {
            types[i] = parseFunctionType();
        }
        return types;
    }

    private Descriptor parseDescriptor() {
        byte ib = bb.get();
        if (ib > 3) {
            System.out.println("Invalid Descriptor type: " + ib);
            return null;
        }
        Descriptor desc = null;
        switch (ib) {
            case 0 -> {
                // Function
                int typeIdx = (int) Leb128.readUnsigned(bb);
                desc = new Descriptor.FunctionDescriptor(typeIdx);
            }
            case 1 -> {
                // Table
                var refType = ValueType.of(bb.get());
                byte fb = bb.get();
                int min = (int) Leb128.readUnsigned(bb);
                int max = (fb == 1) ? (int) Leb128.readUnsigned(bb) : Memory.MAX_PAGES;
                desc = new Descriptor.TableDescriptor(refType, min, max);
            }
            case 2 -> {
                // Memory
                byte fb = bb.get();
                int min = (int) Leb128.readUnsigned(bb);
                int max = (fb == 1) ? (int) Leb128.readUnsigned(bb) : Memory.MAX_PAGES;
                desc = new Descriptor.MemoryDescriptor(min, max);
            }
            case 3 -> {
                // Global
                var valType = ValueType.of(bb.get());
                boolean mutable = bb.get() == 1;
                desc = new Descriptor.GlobalDescriptor(valType, mutable);
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
        Descriptor desc = parseDescriptor();
        return new ImportMetadata(new String(moduleName, StandardCharsets.UTF_8), new String(name, StandardCharsets.UTF_8), desc);
    }

    private ImportMetadata[] parseImports(int numBytes) {
        if (numBytes <= 0) {
            return null;
        }
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
        Descriptor desc = parseDescriptor();
        return new ExportMetadata(new String(name, StandardCharsets.UTF_8), desc);
    }

    private ExportMetadata[] parseExports(int numBytes) {
        if (numBytes <= 0) {
            return null;
        }
        int n = (int) Leb128.readUnsigned(bb);
        ExportMetadata[] exports = new ExportMetadata[n];
        for (int i = 0; i < n; i++) {
            exports[i] = parseExport();
        }
        return exports;
    }

    private int[] parseFunctionSection(int numBytes) {
        if (numBytes <= 0) {
            return null;
        }
        int numFunctions = (int) Leb128.readUnsigned(bb);
        int[] functions = new int[numFunctions];
        for (int i = 0; i < numFunctions; i++) {
            functions[i] = (int) Leb128.readUnsigned(bb);
        }
        return functions;
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
        int[] functions = new int[0];
        while (bb.hasRemaining()) {
            SectionType st = getSectionType(bb.get());
            int sectionLength = (int) Leb128.readUnsigned(bb);
            System.out.printf("Section: %s Length: %d\n", st, sectionLength);
            // Just skipping for now
            switch (st) {
                case CUSTOM, TABLE, MEMORY, GLOBAL, START, ELEMENT,
                        CODE, DATA, DATA_COUNT -> bb.position((bb.position() + sectionLength));
                case TYPE -> types = parseTypes(sectionLength);
                case IMPORT -> imports = parseImports(sectionLength);
                case EXPORT -> exports = parseExports(sectionLength);
                case FUNCTION -> functions = parseFunctionSection(sectionLength);
            }
        }
        System.out.println(bb.position());
        System.out.println(Arrays.toString(types));
        System.out.println(Arrays.toString(imports));
        System.out.println(Arrays.toString(exports));
        System.out.println(Arrays.toString(functions));
        return new Module(1, types, null, null, exports, imports);
    }

    public static WasmParser fromFile(String path) throws IOException {
        byte[] source = FileUtils.readBinaryFile(path);
        return new WasmParser(source);
    }

    public static void main(String[] args) throws Exception {
        String f1 = "./examples/empty_module.wasm";
        String f2 = "./examples/import_global.wasm";
        String f3 = "./examples/add_two.wasm";
        String path = Paths.get(f3).toAbsolutePath().normalize().toString();
        System.out.println("Path: " + path);
        byte[] data = FileUtils.readBinaryFile(path);
        System.out.println("Data read: " + data.length);
        System.out.println("B0: " + bytesToHex(Arrays.copyOf(data, 16)));
        WasmParser wp = fromFile(path);
        wp.parseModule();
    }
}
