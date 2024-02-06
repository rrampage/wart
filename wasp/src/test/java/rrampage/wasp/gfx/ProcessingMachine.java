package rrampage.wasp.gfx;

import processing.core.PApplet;
import processing.core.PImage;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitor;

import java.nio.ByteBuffer;
import java.util.Map;

import static rrampage.wasp.TestUtils.parseModule;

public abstract class ProcessingMachine extends PApplet {
    final Module module;
    Machine machine;

    ProcessingMachine(String filename) {
        module = parseModule(filename);
    }

    ProcessingMachine(String filename, Map<String, Map<String, Object>> importMap, MachineVisitor visitor) {
        this(filename);
        machine = module.instantiate(importMap, visitor);
    }

    protected void updateImagePixels(PImage image, ByteBuffer buffer, int position) {
        buffer.position(position);
        image.loadPixels();
        for (int i = 0; i < image.pixels.length; i++) {
            var r = Byte.toUnsignedInt(buffer.get());
            var g = Byte.toUnsignedInt(buffer.get());
            var b = Byte.toUnsignedInt(buffer.get());
            var a = Byte.toUnsignedInt(buffer.get());
            image.pixels[i] = color(r, g, b, a);
        }
        image.updatePixels();
    }

    public void run() {
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }
}
