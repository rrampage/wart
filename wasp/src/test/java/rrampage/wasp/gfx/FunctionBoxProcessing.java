package rrampage.wasp.gfx;

import rrampage.wasp.utils.ConversionUtils;
import rrampage.wasp.vm.MachineVisitors;

import java.util.ArrayList;

public class FunctionBoxProcessing extends ProcessingMachine {
    record FunctionBox(int x, int y, int w, int h, String name){}
    ArrayList<FunctionBox> functionBoxes = new ArrayList<>();

    public FunctionBoxProcessing() {
        super("../wart/examples/testsuite/fac.0.wasm", null, MachineVisitors.instructionCountVisitor());
    }

    public void settings() {
        size(900, 900, P2D);
    }

    public void setup() {
        background(255);
        int x = 10;
        int y = 50;
        // TODO : store (x,y) + string in array
        for (var e : machine.exports().entrySet()) {
            fill(234, 189, 127);
            if (x >= width - 60) {
                x = 10;
                y += 50;
            }
            x += 70;
            square(x, y, 50);
            functionBoxes.add(new FunctionBox(x, y, 50, 50, e.getKey()));
            textSize(12);
            fill(0, 0, 0);
            text(e.getKey(), x+2, y+4, 45, 45);
        }
    }

    public void draw() {
        update(mouseX, mouseY);
    }

    boolean overRect(int mx, int my, int x, int y, int width, int height)  {
        return mx >= x && mx <= x + width &&
                my >= y && my <= y + height;
    }

    public void update(int x, int y) {
        for (var fb : functionBoxes) {
            if (overRect(x, y, fb.x, fb.y, fb.w, fb.h)) {
                fill(255, 255, 255);
                rect(300, 400, 500, 200);
                System.out.println("Invoking: " + fb.name);
                textSize(30);
                fill(0, 0, 0);
                text("Calling function: " + fb.name(), 320, 430, 400, 120);
                machine.invoke(fb.name, ConversionUtils.constOf(14));
                System.out.println("Invoked: " + fb.name);
            }
        }
    }

    public static void main(String[] args) {
        new FunctionBoxProcessing().run();
    }
}
