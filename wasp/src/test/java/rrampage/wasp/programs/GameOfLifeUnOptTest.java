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
    Module module = parseModule("./game_of_life_unopt.wasm");
    Machine machine;
    byte[][] referenceArray;
    byte[][] machineArray;

    @BeforeEach
    public void setup() {
        System.out.println("SETUP RUN!!");
        machine = module.instantiate(null, MachineVisitors.NULL_VISITOR);
        referenceArray = pulsar();
        initializeMachineWithBoard(referenceArray);
    }

    @Test
    public void shouldCorrectlySetupRunGameOfLife() {
        assertTrue(compareWithReferenceArray(referenceArray));
    }

    @Test
    public void shouldCorrectlyIterateGameOfLife() {
        machine.invoke("tick");
        calculateGameOfLife(referenceArray);
        assertTrue((compareWithReferenceArray(referenceArray)));
    }

    public static int[] pulsarIterations() {
        return new int[]{5, 10, 15};
    }

    @ParameterizedTest
    @MethodSource(value =  "pulsarIterations")
    public void shouldCorrectlyIterateGameOfLifeNTimes(int n) {
        for (int i = 0; i < n; i++) {
            machine.invoke("tick");
            calculateGameOfLife(referenceArray);
            assertTrue((compareWithReferenceArray(referenceArray)));
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
            printBoard(board);
            printBoard(machineArray);
            System.out.println("Mismatches: " + misMatches);
        }
        return ret;
    }

    private static int safeVal(byte[][] matrix, int i, int j) {
        return ( i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length ) ? 0 : matrix[i][j]%2;
    }

    public static void calculateGameOfLife(byte[][] matrix) {
        if (matrix == null || matrix.length == 0) { return;}
        int matRows = matrix.length;
        int matCols = matrix[0].length;
        // TODO: Make one pass
        for (int i = 0; i < matRows; i++) {
            for (int j = 0; j < matCols; j++) {
                int liveCells = safeVal(matrix, i-1,j-1)
                        + safeVal(matrix, i-1, j)
                        + safeVal(matrix, i-1, j+1)
                        + safeVal(matrix, i,j-1)
                        + safeVal(matrix, i, j+1)
                        + safeVal(matrix, i+1, j-1)
                        + safeVal(matrix, i+1, j)
                        + safeVal(matrix, i+1, j+1);
                boolean isLive = (matrix[i][j] == 1) ? (liveCells == 2 || liveCells == 3) : (liveCells == 3);
                matrix[i][j] = (isLive) ? (byte) (2 + matrix[i][j]) : matrix[i][j];
            }
        }
        for (int i = 0; i < matRows; i++) {
            for (int j = 0; j < matCols; j++) {
                matrix[i][j] = (byte) (matrix[i][j] >> 1);
            }
        }
    }

    public static void printBoard(byte[][] matrix) {
        if (matrix == null || matrix.length == 0) { return;}
        StringBuilder sb = new StringBuilder();
        for (byte[] bytes : matrix) {
            for (byte b : bytes) {
                sb.append((b % 2 == 0) ? '-' : 'X');
            }
            sb.append('\n');
        }
        System.out.println(sb);
    }

    public static byte[][] pulsar() {
        byte[][] pulsar = new byte[17][17];
        int[] seed = new int[] {2, 7, 9, 14};
        for (int j = 4; j < 7; j++) {
            for (int i : seed) {
                pulsar[i][j] = 1;
                pulsar[i][16-j] = 1;
            }
        }
        for (int i = 4; i < 7; i++) {
            for (int j : seed) {
                pulsar[i][j] = 1;
                pulsar[16-i][j] = 1;
            }
        }
        return pulsar;
    }
}
