package rrampage.wasp.programs;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static rrampage.wasp.TestUtils.*;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeUnOptTest {
    Module module = parseModule("./gol/game_of_life_unopt.wasm");
    Machine machine;
    byte[][] referenceArray;
    byte[][] machineArray;

    @BeforeEach
    public void setup() {
        System.out.println("SETUP RUN!!");
        machine = module.instantiate(null, MachineVisitors.NULL_VISITOR);
        referenceArray = GameOfLifeUtils.pulsar();
        initializeMachineWithBoard(referenceArray);
    }

    @Test
    public void shouldCorrectlySetupRunGameOfLife() {
        assertTrue(compareWithReferenceArray(referenceArray));
    }

    @Test
    public void shouldCorrectlyIterateGameOfLife() {
        machine.invoke("tick");
        GameOfLifeUtils.calculateGameOfLife(referenceArray);
        assertTrue((compareWithReferenceArray(referenceArray)));
    }

    public static int[] pulsarIterations() { return new int[]{5, 10, 15};}

    @ParameterizedTest
    @MethodSource(value =  "pulsarIterations")
    public void shouldCorrectlyIterateGameOfLifeNTimes(int n) {
        for (int i = 0; i < n; i++) {
            machine.invoke("tick");
            GameOfLifeUtils.calculateGameOfLife(referenceArray);
            assertTrue((compareWithReferenceArray(referenceArray)));
        }
    }

    public static int[] javaPulsarIterations() {return new int[]{500, 1000, 1500};}
    @ParameterizedTest
    @MethodSource(value =  "javaPulsarIterations")
    public void shouldIterateGameOfLifeJavaNTimes(int n) {
        for (int i = 0; i < n; i++) {
            long start = System.nanoTime();
            GameOfLifeUtils.calculateGameOfLife(referenceArray);
            System.out.println("Time taken: " + (System.nanoTime() - start)/1000 + "us");
        }
    }

    private void initializeMachineWithBoard(byte[][] board) {
        int height = board.length;
        int width = board[0].length;
        machine.invoke("initializeBoard", constOf(width), constOf(height));
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(board[row][column]));
            }
        }
        machineArray = new byte[height][width];
    }

    private boolean compareWithReferenceArray(byte[][] board) {
        int height = board.length;
        int width = board[0].length;
        boolean ret = true;
        int misMatches = 0;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                machine.invoke("getValueAtPosition", constOf(row), constOf(column));
                byte b = (byte) machine.popInt();
                machineArray[row][column] = b;
                if (b !=  board[row][column]) {
                    ret = false;
                    misMatches++;
                    System.out.printf("Check failed for row: %d col: %d machineVal: %d refVal: %d\n", row, column, b, referenceArray[row][column]);
                }
            }
        }
        if (!ret) {
            GameOfLifeUtils.printBoard(board);
            GameOfLifeUtils.printBoard(machineArray);
            System.out.println("Mismatches: " + misMatches);
        }
        return ret;
    }
}
