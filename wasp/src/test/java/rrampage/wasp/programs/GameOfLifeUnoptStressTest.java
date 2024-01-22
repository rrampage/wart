package rrampage.wasp.programs;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static rrampage.wasp.TestUtils.parseModule;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeUnoptStressTest {
    private static final Random rng = ThreadLocalRandom.current();
    private static final GolTestCase[] testCases = new GolTestCase[] {
            new GolTestCase(800, 600, 0.4f, 10)
    };

    record GolTestCase(int width, int height, float prob, int iterations){
        public void check() {
            byte[][] board = new byte[height][width];
            Module module = parseModule("./game_of_life_unopt.wasm");
            Machine machine = module.instantiate(null, MachineVisitors.NULL_VISITOR);
            machine.invoke("initializeBoard", constOf(width), constOf(height));
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    byte filled = (byte) (rng.nextFloat() < prob ? 1 : 0);
                    board[row][column] = filled;
                    machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(filled));
                }
            }
            long start = System.nanoTime();
            for (int i = 0; i < iterations*10; i++) {
                GameOfLifeUtils.calculateGameOfLife(board);
            }
            System.out.println("TICK_JAVA Average Time taken: " + (System.nanoTime() - start)/(1000* 10L *iterations) + "us");
            for (int i = 0; i < iterations; i++) {
                start = System.nanoTime();
                machine.invoke("tick");
                System.out.println("TICK_WASM Time taken: " + (System.nanoTime() - start)/1000 + "us");
            }
        }
    }

    @TestFactory
    @Tag("high-resource")
    public Stream<DynamicTest> testGolUnoptimized() {
        return DynamicTest.stream(Stream.of(testCases), GolTestCase::toString, GolTestCase::check);
    }
}
