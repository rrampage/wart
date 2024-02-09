package rrampage.wasp.testsuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.WastCommand;
import rrampage.wasp.data.WastJson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;


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

    public static Map<String, List<AssertReturn>> getTestCases(WastCommand[] commands) {
        Map<String, List<AssertReturn>> testMap = new HashMap<>();
        List<AssertReturn> testList = new ArrayList<>();
        String module = "";
        for (var wc : commands) {
            switch (wc) {
                case WastCommand.WastModule mod -> {
                    if (!module.isBlank() && !testList.isEmpty()) {
                        testMap.put(module, testList);
                    }
                    testList = new ArrayList<>();
                    module = mod.getFilename();
                }
                case WastCommand.WastAction ret -> {
                    try {
                        testList.add(ret.toAssert());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                case WastCommand.WastReturn ret -> {
                    try {
                        testList.add(ret.toAssert());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                default -> {}
            }
        }
        if (!module.isBlank() && !testList.isEmpty()) {
            testMap.put(module, testList);
        }
        return testMap;
    }

    public static Map<String, List<AssertReturn>> parseTestCases(String filename) {
        return getTestCases(parseWastJson(filename));
    }

    public static void main(String[] args) throws Exception {
        String filePath = "examples/testsuite/f32.json";
        System.out.println(Paths.get(filePath).toAbsolutePath().normalize());
        File file = new File(filePath);
        WastJson wastJson = om.readValue(file, WastJson.class);
        System.out.println(wastJson.getSourceFilename());
        getTestCases(wastJson.getCommands());
    }
}
