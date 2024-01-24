package rrampage.wasp.programs;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeUtils {
    private static final Random rng = ThreadLocalRandom.current();
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

    public static byte[][] generateRandomGrid(int height, int width, double probabilityIsAlive) {
        byte[][] board = new byte[height][width];
        double[] rands = rng.doubles().limit(height*width).toArray();
        int i = 0;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                board[row][column] = (byte) (rands[i] < probabilityIsAlive ? 1 : 0);
                i++;
            }
        }
        return board;
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
