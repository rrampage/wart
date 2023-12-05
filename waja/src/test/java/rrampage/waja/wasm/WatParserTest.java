package rrampage.waja.wasm;

import org.junit.Test;
import rrampage.waja.utils.FileUtils;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class WatParserTest {
    private static final String examplesDir = "../examples";

    private static String getFilePath(String fileName) {
        return Paths.get(examplesDir, fileName).toAbsolutePath().normalize().toString();
    }

    @Test
    public void shouldReadFileAndCreateParser() {
        String program = FileUtils.readFile(getFilePath("rocket.wat"));
        assertNotEquals(program, "");
        WatParser parser = new WatParser(program);
        System.out.println(program);
    }
}
