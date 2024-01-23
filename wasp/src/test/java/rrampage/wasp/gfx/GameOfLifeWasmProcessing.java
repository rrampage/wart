package rrampage.wasp.gfx;

import rrampage.wasp.programs.GameOfLifeUtils;
import rrampage.wasp.vm.MachineVisitor;
import rrampage.wasp.vm.MachineVisitors;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeWasmProcessing extends ProcessingMachine {
    int width = 17;
    int height = 17;
    int pixelSize = 17;
    int interval = 100;
    int lastRecordedTime = 0;
    int aliveColor = color(0, 200, 0);
    int deadColor = color(0);
    int iter = 0;
    // Pause
    boolean pause = false;

    GameOfLifeWasmProcessing(MachineVisitor visitor) {
        super("../wart/examples/game_of_life_unopt.wasm", null, visitor);
    }

    public void settings() {
        size(width*pixelSize, height*pixelSize, P2D);
        noSmooth();
    }

    public void setup() {
        System.out.println("SETUP called");
        // Fill in black in case cells don't cover all the windows
        // background(0);
        initialize(width, height);
        // stroke(48);
        drawBoard();
        // noLoop();
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
        var pulsar = GameOfLifeUtils.pulsar();
        height = pulsar.length;
        width = pulsar[0].length;
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                // var filled = Math.random() > .5 ? 1 : 0;
                var filled = pulsar[row][column];
                // System.out.printf("setValueAtPosition(%d, %d, %d)\n", row, column, filled);
                machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(filled));
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
        new GameOfLifeWasmProcessing(MachineVisitors.NULL_VISITOR).run();
    }
}
