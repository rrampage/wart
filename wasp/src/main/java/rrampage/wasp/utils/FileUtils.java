package rrampage.wasp.utils;

import java.io.IOException;
import java.nio.file.*;

public class FileUtils {
    public static String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    public static byte[] readBinaryFile(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    public static String getCurrentDirectory() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public static boolean writeBinaryFile(String path, byte[] data) {
        try {
            Files.write(Path.of(path), data);
            return true;
        } catch (IOException e) {
            System.out.println(STR."FILE_UTILS_WRITE_FILE_ERROR: \{e.getMessage()}");
            return false;
        }
    }
}
