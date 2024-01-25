package rrampage.wasp.testsuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.WastCommand;
import rrampage.wasp.data.WastJson;
import rrampage.wasp.data.WastReturn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;


public class TestSuiteParser {
    private static final ObjectMapper om = new ObjectMapper();

    public static WastCommand[] parseWastJson(String filename) {
        String testSuiteDirectory = "../examples/testsuite/";
        System.out.println(Paths.get(testSuiteDirectory).toAbsolutePath().normalize());
        File file = new File(testSuiteDirectory + filename);
        try {
            return om.readValue(file, WastJson.class).getCommands();
        } catch (IOException e) {
            System.out.println("JSON_READ_ERROR for " + filename + ": " + e.getMessage());
            return new WastCommand[]{};
        }
    }

    public static Stream<AssertReturn> getTestCases(WastCommand[] commands) {
        return Arrays.stream(commands).filter(c -> c instanceof WastReturn).map(w -> ((WastReturn) w).toAssert());
    }

    public static Stream<AssertReturn> parseTestCases(String filename) {
        return getTestCases(parseWastJson(filename));
    }

    public static void main(String[] args) throws Exception {
        String filePath = "examples/testsuite/f32.json";
        System.out.println(Paths.get(filePath).toAbsolutePath().normalize());
        File file = new File(filePath);
        WastJson wastJson = om.readValue(file, WastJson.class);
        System.out.println(wastJson.getSourceFilename());
        getTestCases(wastJson.getCommands()).forEach(a -> System.out.println(a.toString()));
    }
}
