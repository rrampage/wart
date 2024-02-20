#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;


void main() {
    /*
            [0][1]
            [1][0]
    */
    // normalize gl_frag by resolution
    vec2 st = gl_FragCoord.xy/u_resolution;
    float timeMod = mod(u_time, 10.); // [0,9)
    float mx = mod(gl_FragCoord.x/10., timeMod)/timeMod; // 0 -> 1
    float my = mod(gl_FragCoord.y/10., timeMod)/timeMod; // 0 -> 1
    float pctx = step(0.5, mx);
    float pcty = step(0.5, my);
    float pct = abs(pctx - pcty);
    gl_FragColor = vec4(pct, 0, 0,1.0);
}