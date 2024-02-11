package rrampage.wasp.gfx;

import processing.core.PImage;
import rrampage.wasp.data.Memory;
import rrampage.wasp.vm.MachineVisitors;

import java.nio.ByteBuffer;
import java.util.Map;

import static rrampage.wasp.utils.ConversionUtils.constOf;
import static rrampage.wasp.utils.ConversionUtils.intToBytes;

public class RaymarcherProcessing extends ProcessingMachine {
    Memory memory = new Memory(5);
    long ts = System.currentTimeMillis();
    int iterations, frame;
    PImage image;

    RaymarcherProcessing() {
        super("../wart/examples/wasmtoy/raymarcher.wasm");
        machine = module.instantiate(Map.of("js", Map.of("mem", memory)), MachineVisitors.NULL_VISITOR);
    }

    public void settings() { size(320, 200, P2D);}

    public void setup() {
        image = createImage(width, height, ARGB);
        // Push an initial palette to memory
        int baseAddr = 64000;
        for (int i = 0; i < 256; i++) {
            int col = i + (i<<8) + (i<<16) + 0xff000000;
            memory.store(baseAddr + i*4, col);
        }
    }

    private void calculate() {
        float x = -0.5f, y = -0.3125f, rx, ry, rz, qx, qy, qz, d, s, o;
        int idx = 0;
        while (idx < 64000) {
            x = -0.5f;
            while (x < 0.5f) {
                qx = 0.0f;
                qy = 0.0f;
                qz = frame * 0.02f;
                o = 1.0f;
                idx++;
            }
        }
        frame++;
    }

    public void draw() {
        ts = System.currentTimeMillis();
        System.out.println("Invoking wasm_main");
        machine.invoke("wasm_main");
        System.out.println("Invoking palette_to_rgba8");
        machine.invoke("palette_to_rgba8");
        updateImagePixels(image, memory.buffer(), 65536);
        image(image, 0, 0);
        System.out.println(STR."TIME_TAKEN: \{System.currentTimeMillis() - ts} ITERATIONS: \{iterations++}");
    }

    public static void main(String[] args) {
        new RaymarcherProcessing().run();
    }
}
