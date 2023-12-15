package rrampage.wasp.parser;

import rrampage.wasp.data.DataType;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.Module;
import rrampage.wasp.parser.types.*;
import rrampage.wasp.utils.FileUtils;
import rrampage.wasp.utils.Leb128;

import java.io.IOException;
import java.nio.ByteBuffer;
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
        int pos = bb.position();
        int numTypes = (int) Leb128.readUnsigned(bb);
        FunctionType[] types = new FunctionType[numTypes];
        for (int i = 0; i < numTypes; i++) {
            types[i] = parseFunctionType();
        }
        return types;
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
        while (bb.hasRemaining()) {
            SectionType st = getSectionType(bb.get());
            long sectionLength = Leb128.readUnsigned(bb);
            System.out.printf("Section: %s Length: %d\n", st, sectionLength);
            // Just skipping for now
            switch (st) {
                case CUSTOM, IMPORT, FUNCTION,
                        TABLE, MEMORY, GLOBAL,
                        EXPORT, START, ELEMENT,
                        CODE, DATA, DATA_COUNT -> bb.position((int) (bb.position() + sectionLength));
                case TYPE -> types = parseTypes((int) sectionLength);
            }
        }
        System.out.println(bb.position());
        System.out.println(Arrays.toString(types));
        return new Module(1, types, null, null, null, null);
    }

    public static WasmParser fromFile(String path) throws IOException {
        byte[] source = FileUtils.readBinaryFile(path);
        return new WasmParser(source);
    }

    public static void main(String[] args) throws Exception {
        String f1 = "./examples/empty_module.wasm";
        String f2 = "./examples/fizzbuzz_manual.wasm";
        String f3 = "./examples/add_two.wasm";
        String path = Paths.get(f2).toAbsolutePath().normalize().toString();
        System.out.println("Path: " + path);
        byte[] data = FileUtils.readBinaryFile(path);
        System.out.println("Data read: " + data.length);
        System.out.println("B0: " + bytesToHex(Arrays.copyOf(data, 16)));
        WasmParser wp = fromFile(path);
        wp.parseModule();
    }
}
