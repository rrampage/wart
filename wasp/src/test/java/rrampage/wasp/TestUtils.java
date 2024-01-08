package rrampage.wasp;

import rrampage.wasp.data.Module;
import rrampage.wasp.instructions.ConstInstruction;
import rrampage.wasp.parser.WasmParser;
import rrampage.wasp.utils.FileUtils;
import rrampage.wasp.vm.Machine;

import java.io.IOException;
import java.nio.file.Paths;

public class TestUtils {
    private static final String examplesDir = "../examples";

    public static String getFilePath(String fileName) {
        return Paths.get(examplesDir, fileName).toAbsolutePath().normalize().toString();
    }

    public static String readFile(String path) {
        try {
            return FileUtils.readFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readBinaryFile(String path) {
        try {
            return FileUtils.readBinaryFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Module parseModule(String fileName) {
        return new WasmParser(readBinaryFile(getFilePath(fileName))).parseModule();
    }

    public static boolean invokeAndCheckStack(Machine m, String function, ConstInstruction[] args, ConstInstruction[] expected) {
        m.invoke(function, args);
        return m.compareStack(expected);
    }
}
