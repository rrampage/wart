package rrampage.wasp.gfx;

import processing.core.PApplet;
import rrampage.wasp.data.Module;
import rrampage.wasp.utils.ConversionUtils;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitor;
import rrampage.wasp.vm.MachineVisitors;

import java.util.ArrayList;
import java.util.Map;

import static rrampage.wasp.TestUtils.parseModule;

public class ProcessingMachine extends PApplet {

    record FunctionBox(int x, int y, int w, int h, String name){}

    final Module module;
    final Machine machine;
    ArrayList<FunctionBox> functionBoxes = new ArrayList<>();

    public static void main(String[] args) {
        new ProcessingMachine("../wart/examples/testsuite/fac.0.wasm", null, MachineVisitors.instructionCountVisitor()).run();
    }

    ProcessingMachine(String filename, Map<String, Map<String, Object>> importMap, MachineVisitor visitor) {
        module = parseModule(filename);
        machine = module.instantiate(importMap, visitor);
    }

    @Override
    public void settings() {
        size(900, 900, P2D);
    }

    @Override
    public void setup() {
        background(255);
        int x = 10;
        int y = 50;
        // TODO : store (x,y) + string in array
        for (var e : machine.exports().entrySet()) {
            fill(0, 0, 255);
            if (x >= width - 60) {
                x = 10;
                y += 50;
            }
            x += 50;
            square(x, y, 50);
            functionBoxes.add(new FunctionBox(x, y, 50, 50, e.getKey()));
            textSize(10);
            fill(255, 0, 0);
            text(e.getKey(), x+10, y+10, 45, 45);
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
                System.out.println("Invoking: " + fb.name);
                machine.invoke(fb.name, ConversionUtils.constOf(14));
                System.out.println("Invoked: " + fb.name);
            }
        }
    }

    public void run() {
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }
}
