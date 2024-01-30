package rrampage.wasp.gfx;

import processing.core.PImage;
import rrampage.wasp.data.*;
import rrampage.wasp.vm.MachineVisitors;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.*;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class Olive extends ProcessingMachine {
    boolean paused = true;
    final Variable.I32Variable heapBase;
    final Memory memory;
    int pixels, width, height, stride, iterations;
    long ts = System.currentTimeMillis();
    PImage image;
    record Rgba(int r, int g, int b, int a){}

    public Olive() throws RuntimeException {
        super("../wart/examples/olive/triangle.wasm", createImportMap(), MachineVisitors.NULL_VISITOR);
        // super("../wart/examples/olive/simple.wasm", createImportMap(), MachineVisitors.NULL_VISITOR);
        heapBase = (Variable.I32Variable) machine.exports().get("__heap_base");
        memory = (Memory) machine.exports().get("memory");
    }

    public void settings() { size(900, 900, P2D);}
    public void setup() {
        machine.invoke("vc_render", constOf(heapBase.getVal()), constOf(0));
        var buffer = memory.buffer();
        readCanvasFromMemory(buffer, heapBase.getVal());
        image = createImage(width, height, ARGB);
        updateImagePixels(buffer);
        ts = System.currentTimeMillis();
        iterations++;
    }

    private void updateImagePixels(ByteBuffer buffer) {
        HashMap<Rgba, Integer> colorSet = new HashMap<>();
        buffer.position(pixels);
        image.loadPixels();
        for (int i = 0; i < image.pixels.length; i++) {
            var r = Byte.toUnsignedInt(buffer.get());
            var g = Byte.toUnsignedInt(buffer.get());
            var b = Byte.toUnsignedInt(buffer.get());
            var a = Byte.toUnsignedInt(buffer.get());
            var rec = new Rgba(r,g,b,a);
            if (a <= 10) {
                a = 255;
            }
            //var c = color(r, g, b, a);
            var c = color(r, g, b, a);
            colorSet.put(rec, colorSet.compute(rec, (k, v) -> (v == null) ? 1 : v+1)) ;
            image.pixels[i] = c;
        }
        System.out.println(colorSet);
        // System.out.println("Bytes consumed: " + (buffer.position() - pixels) + " Pixel arr: " + image.pixels.length);
        image.updatePixels();
        image(image, 0, 0);
    }

    private void readCanvasFromMemory(ByteBuffer buffer, int canvasPtr) {
        buffer.position(canvasPtr);
        pixels = buffer.getInt();
        width = buffer.getInt();
        height = buffer.getInt();
        stride = buffer.getInt();
        System.out.printf("Pixels %d Width %d Height %d Stride %d\n", pixels, width, height, stride);
        if (width != stride) {
            throw new RuntimeException("FAIL!! Canvas width not equal to stride");
        }
    }

    public void render(float dt) {
        System.out.println("dt: " + dt);
        machine.invoke("vc_render", constOf(heapBase.getVal()), constOf(dt * 0.1f));
        var buffer = memory.buffer();
        readCanvasFromMemory(buffer, heapBase.getVal());
        updateImagePixels(buffer);
        iterations++;
        System.out.println("Iterations: " + iterations);
    }

    public void draw() {
        render(System.currentTimeMillis() - ts);
        ts = System.currentTimeMillis();
    }

    public void mouseEntered() { paused = false;}
    public void mouseExited() { paused = true;}

    private static Map<String, Map<String,Object>> createImportMap() throws RuntimeException {
        try {
            FunctionType type0 = FunctionType.F32_UNARY;
            FunctionType type1 = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F32, ValueType.NumType.F32}, new ValueType.NumType[]{ValueType.NumType.F32});
            MethodType mt = FunctionType.getMethodTypeFromFunctionType(type0);
            var sinMh = Function.createImportFunction("sinf", type0, MethodHandles.lookup().findStatic(Olive.class, "sinf", mt));
            var sqrtMh = Function.createImportFunction("sqrtf", type0, MethodHandles.lookup().findStatic(Olive.class, "sqrtf", mt));
            var cosMh = Function.createImportFunction("cosf", type0, MethodHandles.lookup().findStatic(Olive.class, "cosf", mt));
            var atan2Mh = Function.createImportFunction("atan2f", type1, MethodHandles.lookup().findStatic(Olive.class, "atan2f", FunctionType.getMethodTypeFromFunctionType(type1)));
            return Map.of("env", Map.of("sinf", sinMh, "cosf", cosMh, "atan2f", atan2Mh, "sqrtf", sqrtMh));
        } catch (Exception e) {
            throw new RuntimeException("OLIVE_INIT Method Handle lookup failed");
        }
    }

    public static void main(String[] args) {
        new Olive().run();
    }

    public static float sinf(float a) { return (float) Math.sin(a);}
    public static float cosf(float a) { return (float) Math.cos(a);}
    public static float atan2f(float x, float y) { return (float) Math.atan2(x, y);}
    public static float sqrtf(float a) { return (float) Math.sqrt(a);}
}
