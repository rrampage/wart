package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static rrampage.wasp.TestUtils.invokeAndCheckStack;
import static rrampage.wasp.TestUtils.parseModule;

public class TestSuiteRunner {
    private static final String testSuitePath = "./testsuite/";
    private Machine machine;
    private final Module module;

    public TestSuiteRunner(String modulePath) {
        this(modulePath, null, MachineVisitors.NULL_VISITOR);
    }

    public TestSuiteRunner(String modulePath, Map<String, Map<String, Object>> importMap, MachineVisitor visitor) {
        module = parseModule(modulePath);
        machine = module.instantiate(importMap, visitor);
    }

    public void instantiateMachine(Map<String, Map<String, Object>> importMap, MachineVisitor visitor) {
        this.machine = module.instantiate(importMap, visitor);
    }

    public Machine getMachine() {
        return machine;
    }

    public void check(AssertReturn test) {
        assertTrue(invokeAndCheckStack(machine, test.function(), test.args(), test.expected()));
    }

    public Stream<DynamicTest> test(AssertReturn[] testCases) {
        return test(Stream.of(testCases));
    }

    public Stream<DynamicTest> test(Stream<AssertReturn> testCaseStream) {
        return DynamicTest.stream(testCaseStream, AssertReturn::toString, this::check);
    }

    public static Stream<DynamicTest> wastTest(String moduleName, String wastJsonFile) {
        TestSuiteRunner runner = new TestSuiteRunner(testSuitePath + moduleName);
        var map = TestSuiteParser.parseTestCases(wastJsonFile);
        return DynamicTest.stream(map.get(moduleName).stream(), AssertReturn::toString, runner::check);
    }

    public static Stream<DynamicTest> wastTest(String wastJsonFile) {
        var map = TestSuiteParser.parseTestCases(wastJsonFile);
        var testMap = new HashMap<String, Stream<DynamicTest>>(map.size());
        for (var e : map.entrySet()) {
            try {
                TestSuiteRunner runner = new TestSuiteRunner(testSuitePath + e.getKey());
                testMap.put(e.getKey(), DynamicTest.stream(e.getValue().stream(), AssertReturn::toString, runner::check));
            } catch (RuntimeException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return testMap.entrySet().stream().flatMap(Map.Entry::getValue);
    }
}
