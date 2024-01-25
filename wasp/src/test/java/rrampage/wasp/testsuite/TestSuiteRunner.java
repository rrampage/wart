package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import rrampage.wasp.data.AssertReturn;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.*;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static rrampage.wasp.TestUtils.invokeAndCheckStack;
import static rrampage.wasp.TestUtils.parseModule;

public class TestSuiteRunner {
    private Machine machine;
    private final Module module;

    public TestSuiteRunner(String modulePath) {
        this(modulePath, null, MachineVisitors.logVisitor());
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
        return DynamicTest.stream(Stream.of(testCases), AssertReturn::toString, this::check);
    }
}
