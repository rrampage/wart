package rrampage.wasp.gfx;

import rrampage.wasp.programs.GameOfLifeUtils;
import rrampage.wasp.vm.MachineVisitor;
import rrampage.wasp.vm.MachineVisitors;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeWasmProcessing extends ProcessingMachine {
    private static final String GOL_UNOPT_PATH = "../wart/examples/gol/game_of_life_unopt.wasm";
    private static final String GOL_OPT_PATH = "../wart/examples/gol/game_of_life.wasm";
    final int width;
    final int height;
    final int pixelSize;
    int interval = 100;
    int lastRecordedTime = 0;
    int aliveColor = color(0, 200, 0);
    int deadColor = color(0);
    int iter = 0;
    // Pause
    boolean pause = false;
    final byte[][] startGrid;

    GameOfLifeWasmProcessing(MachineVisitor visitor, byte[][] startGrid, int pixelSize, boolean useOptimized) {
        super((useOptimized) ? GOL_OPT_PATH : GOL_UNOPT_PATH, null, visitor);
        if (startGrid == null || startGrid.length == 0) {
            throw new RuntimeException("Invalid starting grid");
        }
        this.startGrid = startGrid;
        this.height = startGrid.length;
        this.width = startGrid[0].length;
        this.pixelSize = pixelSize;
    }

    public void settings() {
        size(width*pixelSize, height*pixelSize, P2D);
        noSmooth();
    }

    public void setup() {
        System.out.println("SETUP called");
        initialize(width, height);
        drawBoard();
    }

    public void draw() {
        long timeStart = System.nanoTime();
        iter++;
        if (millis() - lastRecordedTime > interval) {
            if (!pause) {
                machine.invoke("tick");
                var timeTakenMs = (System.nanoTime() - timeStart)/1_000_000;
                System.out.printf("TICK_END iteration: %d timeTaken: %d\n", iter, timeTakenMs);
                lastRecordedTime = millis();
            }
        }
        drawBoard();
    }

    void initialize(int width, int height) {
        machine.invoke("initializeBoard", constOf(width), constOf(height));
        System.out.println("Board INIT");
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(startGrid[row][column]));
            }
        }
    }

    void drawBoard() {
        //const { gameExports, width, height, pixelSize, ctx, canvas } = gameState;
        rect(0, 0, width*pixelSize, height*pixelSize);
        //beginShape();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                machine.invoke("getValueAtPosition", constOf(row), constOf(column));
                var i = machine.popInt();
                // System.out.println("IIII " + i);
                boolean alive = i != 0;
                fill(alive ? aliveColor : deadColor);
                var x = column * pixelSize;
                var y = row * pixelSize;
                square(x, y, pixelSize);
            }
        }
    }

    public static void main(String[] args) {
        new GameOfLifeWasmProcessing(MachineVisitors.NULL_VISITOR, GameOfLifeUtils.pulsar(), 17, true).run();
        //new GameOfLifeWasmProcessing(MachineVisitors.NULL_VISITOR, GameOfLifeUtils.generateRandomGrid(600, 800, 0.4), 2, true).run();
    }
}
