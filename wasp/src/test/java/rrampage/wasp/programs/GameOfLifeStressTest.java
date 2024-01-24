package rrampage.wasp.programs;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import java.util.stream.Stream;

import static rrampage.wasp.TestUtils.parseModule;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeStressTest {
    private static final String golOptimized ="./gol/game_of_life.wasm";
    private static final String golUnoptimized = "./gol/game_of_life_unopt.wasm";
    private static final GolTestCase[] testCases = new GolTestCase[] {
            new GolTestCase(100, 100, 0.4f, 10, false),
            new GolTestCase(100, 100, 0.4f, 10, true),
            new GolTestCase(800, 600, 0.4f, 10, false),
            new GolTestCase(800, 600, 0.4f, 10, true),
    };

    record GolTestCase(int width, int height, float prob, int iterations, boolean useOptimized){
        private void initializeMachine(Machine machine, byte[][] board) {
            machine.invoke("initializeBoard", constOf(width), constOf(height));
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(board[row][column]));
                }
            }
        }
        private void tick(Machine machine) {
            long start;
            for (int i = 0; i < iterations; i++) {
                start = System.nanoTime();
                machine.invoke("tick");
                System.out.println("TICK_WASM Time taken: " + (System.nanoTime() - start)/1000 + "us");
            }
        }
        public void check() {
            byte[][] board = GameOfLifeUtils.generateRandomGrid(height, width, prob);
            var module = parseModule(useOptimized ? golOptimized : golUnoptimized);
            Machine machine = module.instantiate(null, MachineVisitors.NULL_VISITOR);
            initializeMachine(machine, board);
            int javaIters = 1000;
            long start = System.nanoTime();
            for (int i = 0; i < javaIters; i++) {
                GameOfLifeUtils.calculateGameOfLife(board);
            }
            System.out.println("TICK_JAVA Average Time taken: " + (System.nanoTime() - start)/(1000* javaIters) + "us");
            tick(machine);
        }
    }

    @TestFactory
    @Tag("high-resource")
    public Stream<DynamicTest> testGolUnoptimized() {
        return DynamicTest.stream(Stream.of(testCases), GolTestCase::toString, GolTestCase::check);
    }
}
