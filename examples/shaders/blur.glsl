#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;

float plot(vec2 st, float pct) {
	// return smoothstep(0.02, 0.0, abs(st.y - st.x));
	return  smoothstep( pct-0.02, pct, st.y) - smoothstep( pct, pct+0.02, st.y);
}

void main() {
	vec2 st = gl_FragCoord.xy/u_resolution;
	vec2 mt = u_mouse/u_resolution;
	float y = pow(st.x,5.0);
	// gl_FragColor = vec4(abs(sin(u_time)),st.x*mt.x,st.y*mt.y,1.0) + pct;
	vec3 color = vec3(y);

	float pct = plot(st,y);
	color = (1.0-pct)*color+pct*vec3(abs(sin(u_time)),1.0,0.0);
	gl_FragColor = vec4(color,1.0);
}
