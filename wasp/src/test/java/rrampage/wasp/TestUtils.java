package rrampage.wasp;

import rrampage.wasp.utils.FileUtils;

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
}
