package rrampage.wasp.gfx;

import processing.core.PImage;
import processing.event.KeyEvent;
import rrampage.wasp.data.Function;
import rrampage.wasp.data.FunctionType;
import rrampage.wasp.data.Memory;
import rrampage.wasp.vm.MachineVisitors;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class DoomProcessing extends ProcessingMachine {
    long start = System.currentTimeMillis();
    Memory memory = new Memory(103);
    PImage image;
    int iterations, numFrames;
    public DoomProcessing() {
        super("../wart/examples/doom/doom.wasm");
        machine = module.instantiate(createImportMap(), MachineVisitors.NULL_VISITOR);
    }

    public void settings() { size(640, 400, P2D);}

    public void setup() {
        image = createImage(width, height, ARGB);
        image(image, 0, 0);
        machine.invoke("main", constOf(0), constOf(0));
    }

    public void draw() {
        machine.invoke("doom_loop_step");
        numFrames++;
    }

    public void keyPressed(KeyEvent event) {
        System.out.printf("KeyPress: char %c keyCode %d\n", event.getKey(), event.getKeyCode());
        machine.invoke("add_browser_event", constOf(0), constOf(doomKeyCode(event.getKeyCode())));
    }

    public void keyReleased(KeyEvent event) {
        machine.invoke("add_browser_event", constOf(1), constOf(doomKeyCode(event.getKeyCode())));
    }

    private int doomKeyCode(int keyCode) {
        return switch (keyCode) {
            case 8 -> 127; // KEY_BACKSPACE
            case 10 -> 13; // ENter ??
            case 17 -> 0x80+0x1d; // KEY_RCTRL
            case 18 -> 0x80+0x38; // KEY_RALT
            case 37 -> 0xac; // KEY_LEFTARROW
            case 38 -> 0xad; // KEY_UPARROW
            case 39 -> 0xae; // KEY_RIGHTARROW
            case 40 -> 0xaf; // KEY_DOWNARROW
            default -> {
                if (keyCode >= 65 /*A*/ && keyCode <= 90 /*Z*/) {
                    yield keyCode + 32; // ASCII to lower case
                }
                if (keyCode >= 112 /*F1*/ && keyCode <= 123 /*F12*/ ) {
                    yield keyCode + 75; // KEY_F1
                }
                yield keyCode;
            }
        };
    }

    private Map<String, Map<String, Object>> createImportMap() {
        /*
             - func[0] () -> i32 <js.js_milliseconds_since_start> <- js.js_milliseconds_since_start
             - func[1] (i32, i32) -> nil <js.js_console_log> <- js.js_console_log
             - func[2] (i32) -> nil <js.js_draw_screen> <- js.js_draw_screen
             - func[3] (i32, i32) -> nil <js.js_stdout> <- js.js_stdout
             - func[4] (i32, i32) -> nil <js.js_stderr> <- js.js_stderr
             - memory[0] pages: initial=102 <- env.memory
         */
        try {
            var millisSinceStartMh = Function.createImportFunction("js_milliseconds_since_start", FunctionType.I32_RETURN,
                    MethodHandles.lookup().findVirtual(this.getClass(), "millisSinceStart", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_RETURN)).bindTo(this));
            var jsDrawScreenMh = Function.createImportFunction("js_draw_screen", FunctionType.I32_CONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "jsDrawScreen", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_CONSUME)).bindTo(this));
            var jsConsoleLogMh = Function.createImportFunction("js_console_log", FunctionType.I32_BICONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "jsConsoleLog", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_BICONSUME)).bindTo(this));
            var jsStdOutLogMh = Function.createImportFunction("js_stdout", FunctionType.I32_BICONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "jsStdOut", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_BICONSUME)).bindTo(this));
            var jsStdErrLogMh = Function.createImportFunction("js_stderr", FunctionType.I32_BICONSUME,
                    MethodHandles.lookup().findVirtual(this.getClass(), "jsStdErr", FunctionType.getMethodTypeFromFunctionType(FunctionType.I32_BICONSUME)).bindTo(this));
            return Map.of("env", Map.of("memory", memory),
                    "js", Map.of("js_milliseconds_since_start", millisSinceStartMh, "js_draw_screen", jsDrawScreenMh,
                            "js_console_log", jsConsoleLogMh, "js_stdout", jsStdOutLogMh, "js_stderr", jsStdErrLogMh));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int millisSinceStart() {
        return (int) (System.currentTimeMillis() - start);
    }

    private void jsDrawScreen(int ptr) {
        updateImagePixels(image, memory.buffer(), ptr);
        image(image, 0, 0);
        iterations++;
    }

    private void jsStdOut(int addr, int len) {
        System.out.println(STR."STD_OUT: \{readWasmString(addr, len)}");
    }
    private void jsStdErr(int addr, int len) {
        System.out.println(STR."STD_ERR: \{readWasmString(addr, len)}");
    }
    private void jsConsoleLog(int addr, int len) {
        System.out.println(STR."CONSOLE_LOG: \{readWasmString(addr, len)}");
    }

    private String readWasmString(int addr, int len) {
        return new String(memory.load(addr, len), StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        new DoomProcessing().run();
    }
}
