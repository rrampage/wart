package rrampage.wasp.testsuite;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static rrampage.wasp.testsuite.TestSuiteRunner.wastTest;

public class ModuleTest {
    @TestFactory
    public Stream<DynamicTest> testNames() {return wastTest("names.json");}
}
