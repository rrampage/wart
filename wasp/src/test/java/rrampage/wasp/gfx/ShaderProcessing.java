package rrampage.wasp.gfx;

import processing.core.PApplet;
import processing.opengl.PShader;
import rrampage.wasp.utils.FileUtils;

public class ShaderProcessing extends PApplet {
    public void settings() {
        size(640, 360, P2D);
    }

    PShader blur;

    public void setup() {
        System.out.println(FileUtils.getCurrentDirectory());
        // Shaders files must be in the "data" folder to load correctly
        blur = loadShader("examples/shaders/checker.glsl");
        noStroke();
    }

    public void draw() {
        blur.set("u_resolution", width + 0.0f, height + 0.0f);
        blur.set("u_time", millis() / 1000.0f);
        blur.set("u_mouse", mouseX + 0f, mouseY + 0f);
        shader(blur);
        rect(0,0, width, height);
        /*rect(mouseX-75, mouseY, 150, 150);
        ellipse(mouseX+75, mouseY, 150, 150);*/
    }
    public static void main(String[] args) {
        PApplet.main(ShaderProcessing.class.getName());
    }
}
