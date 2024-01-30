package rrampage.wasp.gfx;

import processing.event.KeyEvent;
import rrampage.wasp.data.*;
import rrampage.wasp.vm.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

import static rrampage.wasp.utils.ConversionUtils.constOf;

public class RocketWasmProcessing extends ProcessingMachine {
    long last;
    MachineVisitor visitor;
    RocketWasmProcessing(MachineVisitor visitor) {
        super("../wart/examples/rocket.wasm");
        this.visitor = visitor;
    }
    double highScore = 0.0;

    public void settings() {size(800, 600, P2D);}
    public void setup() {
        machine = module.instantiate(createImportMap(), visitor);
        machine.invoke("resize", constOf(width + 0.0), constOf(height + 0.0));
        last = System.currentTimeMillis();
    }

    public void draw() {
        var now = System.currentTimeMillis();
        var diff = now - last;
        last = now;
        var dt = (diff > 5) ? diff * 0.001 : 0.01 * diff;
        machine.invoke("update", constOf(dt));
        machine.invoke("draw");
    }

    public void clear() { background(color(255, 255, 255));}
    public void drawBullet(double x, double y) {
        fill(color(255, 0, 0));
        circle((float) x, (float) y, 2);
    }
    public void drawEnemy(double x, double y) {
        fill(color(0, 255, 0));
        ellipse((float) x, (float) y, 15, 10);
    }
    public void drawParticle(double x, double y, double z) {
        fill(color(255, 255, 255));
        circle((float) x, (float) y, (float) z);
    }
    public void drawPlayer(double x, double y, double z) {
        fill(color(0, 255, 255));
        circle((float) x, (float) y, 20);
    }

    public void drawScore(double score) {
        highScore = Math.max(score, highScore);
        fill(color(0, 0, 0));
        textSize(20);
        text(String.format("Score is %f", score), 10, 50);
        textSize(10);
        text(String.format("High Score: %f", highScore), 10, 80);
    }

    private Map<String, Map<String,Object>> createImportMap() throws RuntimeException {
        try {
            FunctionType f64Unary = FunctionType.F64_UNARY;
            FunctionType aVoid = FunctionType.VOID;
            FunctionType f64BiConsumer = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64}, new ValueType.NumType[]{});
            FunctionType f64TriConsumer = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64, ValueType.NumType.F64}, new ValueType.NumType[]{});
            FunctionType f64Consume = FunctionType.F64_CONSUME;
            MethodType mt = FunctionType.getMethodTypeFromFunctionType(f64Unary);
            var sinMh = Function.createImportFunction("sin", f64Unary, MethodHandles.lookup().findStatic(Math.class, "sin", mt));
            var cosMh = Function.createImportFunction("cos", f64Unary, MethodHandles.lookup().findStatic(Math.class, "cos", mt));
            var atanMh = Function.createImportFunction("Math_atan", f64Unary, MethodHandles.lookup().findStatic(Math.class, "atan", mt));
            var clrScrMh = Function.createImportFunction("clear_screen", aVoid,
                    MethodHandles.lookup().findVirtual(this.getClass(), "clear", FunctionType.getMethodTypeFromFunctionType(aVoid)).bindTo(this));
            var drawBulletMh = Function.createImportFunction("draw_bullet", f64BiConsumer,
                    MethodHandles.lookup().findVirtual(this.getClass(), "drawBullet", FunctionType.getMethodTypeFromFunctionType(f64BiConsumer)).bindTo(this));
            var drawEnemyMh = Function.createImportFunction("draw_enemy", f64BiConsumer,
                    MethodHandles.lookup().findVirtual(this.getClass(), "drawEnemy", FunctionType.getMethodTypeFromFunctionType(f64BiConsumer)).bindTo(this));
            var drawParticleMh = Function.createImportFunction("draw_particle", f64TriConsumer,
                    MethodHandles.lookup().findVirtual(this.getClass(), "drawParticle", FunctionType.getMethodTypeFromFunctionType(f64TriConsumer)).bindTo(this));
            var drawPlayerMh = Function.createImportFunction("draw_player", f64TriConsumer,
                    MethodHandles.lookup().findVirtual(this.getClass(), "drawPlayer", FunctionType.getMethodTypeFromFunctionType(f64TriConsumer)).bindTo(this));
            var drawScoreMh = Function.createImportFunction("draw_score", f64Consume,
                    MethodHandles.lookup().findVirtual(this.getClass(), "drawScore", FunctionType.getMethodTypeFromFunctionType(f64Consume)).bindTo(this));
            return Map.of("env", Map.of("sin", sinMh, "cos", cosMh, "Math_atan", atanMh,
                    "clear_screen", clrScrMh,
                    "draw_bullet", drawBulletMh,
                    "draw_enemy", drawEnemyMh,
                    "draw_particle", drawParticleMh,
                    "draw_player", drawPlayerMh,
                    "draw_score", drawScoreMh));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("OLIVE_INIT Method Handle lookup failed");
        }
    }

    @Override
    public void mouseMoved() {
        // drawBullet(mouseX, mouseY);
        // drawEnemy(mouseX, mouseY);
        // drawPlayer(mouseX, mouseY, 1);
        drawScore(100);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 32 -> machine.invoke("toggle_shoot", constOf(1));
            case 37 -> machine.invoke("toggle_turn_left", constOf(1));
            case 39 -> machine.invoke("toggle_turn_right", constOf(1));
            case 38 -> machine.invoke("toggle_boost", constOf(1));
            default -> System.out.printf("KeyPress: char %c keyCode %d\n", event.getKey(), event.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case 32 -> machine.invoke("toggle_shoot", constOf(0));
            case 37 -> machine.invoke("toggle_turn_left", constOf(0));
            case 39 -> machine.invoke("toggle_turn_right", constOf(0));
            case 38 -> machine.invoke("toggle_boost", constOf(0));
            default -> System.out.printf("KeyRelease: char %c keyCode %d\n", event.getKey(), event.getKeyCode());
        }
    }

    public static void main(String[] args) {
        new RocketWasmProcessing(MachineVisitors.NULL_VISITOR).run();
    }
}
