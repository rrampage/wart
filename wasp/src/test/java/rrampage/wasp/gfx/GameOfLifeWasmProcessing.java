package rrampage.wasp.gfx;

import processing.core.PApplet;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitor;
import rrampage.wasp.vm.MachineVisitors;

import java.util.ArrayList;
import java.util.Map;

import static rrampage.wasp.TestUtils.parseModule;
import static rrampage.wasp.utils.ConversionUtils.constOf;

public class GameOfLifeWasmProcessing extends PApplet {
    final Module module;
    final Machine machine;
    int width = 640;
    int height = 360;
    int pixelSize = 5;
    int aliveColor = color(0, 200, 0);
    int deadColor = color(0);
    int iter = 0;
    ArrayList<ProcessingMachine.FunctionBox> functionBoxes = new ArrayList<>();

    GameOfLifeWasmProcessing(MachineVisitor visitor) {
        module = parseModule("../wart/examples/game_of_life_unopt.wasm");
        machine = module.instantiate(null, visitor);
    }

    public void settings() {
        size(width, height, P2D);
        noSmooth();
    }

    public void setup() {
        System.out.println("SETUP called");
        // Fill in black in case cells don't cover all the windows
        // background(0);
        initialize(width, height);
        // stroke(48);
        drawBoard();
        noLoop();
    }

    public void draw() {
        System.out.println("DRAW_START");
        drawBoard();
        System.out.println("DRAW_END");
        /*background(0);
        drawBoard();
        machine.invoke("tick");
        iter++;*/
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        */
    }

    public void run() {
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    void initialize(int width, int height) {
        machine.invoke("initializeBoard", constOf(width), constOf(height));
        System.out.println("Board INIT");
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                var filled = Math.random() > .5 ? 1 : 0;
                // System.out.printf("setValueAtPosition(%d, %d, %d)\n", row, column, filled);
                machine.invoke("setValueAtPosition", constOf(row), constOf(column), constOf(filled));
            }
        }
    }

    void drawBoard() {
        //const { gameExports, width, height, pixelSize, ctx, canvas } = gameState;
        rect(0, 0, width, height);
        //beginShape();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                machine.invoke("getValueAtPosition", constOf(row), constOf(column));
                var i = machine.popInt();
                // System.out.println("IIII " + i);
                boolean alive = i != 0;
                if (alive) {
                    System.out.println("ALIVE!! " + iter);
                }
                fill(alive ? aliveColor : deadColor);
                var x = column * pixelSize;
                var y = row * pixelSize;
                square(x, y, pixelSize);
            }
        }
        // TODO : Debug no color
    }

    public static void main(String[] args) {
        GameOfLifeWasmProcessing g = new GameOfLifeWasmProcessing(MachineVisitors.NULL_VISITOR);
        g.run();
    }
}
