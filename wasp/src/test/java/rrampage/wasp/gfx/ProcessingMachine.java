package rrampage.wasp.gfx;

import processing.core.PApplet;
import rrampage.wasp.data.Module;
import rrampage.wasp.vm.Machine;
import rrampage.wasp.vm.MachineVisitor;

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

    public void run() {
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }
}
