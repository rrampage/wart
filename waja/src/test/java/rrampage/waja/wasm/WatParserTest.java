package rrampage.waja.wasm;

import org.junit.jupiter.api.Test;
import rrampage.waja.utils.FileUtils;
import rrampage.waja.wasm.parser.WatParser;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class WatParserTest {
    private static final String examplesDir = "../examples";

    private static String getFilePath(String fileName) {
        return Paths.get(examplesDir, fileName).toAbsolutePath().normalize().toString();
    }

    @Test
    public void shouldReadFile() {
        String program = FileUtils.readFile(getFilePath("rocket.wat"));
        assertNotEquals(program, "");
    }

    @Test
    public void shouldParseEmptyModule() {
        String program = FileUtils.readFile(getFilePath("empty_module.wat"));
        WatParser parser = new WatParser(program);
        Module m = parser.parseModule();
        assertNotEquals(m, null);
    }

    @Test
    public void shouldReadFileAndCreateParser() {
        String program = FileUtils.readFile(getFilePath("add_two.wat"));
        assertNotEquals(program, "");
        WatParser parser = new WatParser(program);
        Module m = parser.parseModule();
        //System.out.println(m.types().length);
        System.out.println(program);
        System.out.println(m);
        assertEquals(3, m.functions().length);
    }

    @Test
    public void shouldParseRocket() {
        String program = FileUtils.readFile(getFilePath("rocket.wat"));
        assertNotEquals(program, "");
        WatParser parser = new WatParser(program);
        Module m = parser.parseModule();
        assertNotEquals(m, null); // till parser impl is completed, just doing a non-null check here
        assertEquals(17, m.types().length);
        assertEquals(125, m.functions().length);
        System.out.println(m.importMap());
    }
}
