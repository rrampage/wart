package rrampage.wasp.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static String readFile(String path) throws IOException {
        return Files.readString(Path.of(path));
    }

    public static byte[] readBinaryFile(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }
}
