#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;

out vec4 fragColor;

uniform vec2 u_resolution;
uniform int u_invertAlpha;

uniform vec2 FADE_LEFT;
uniform vec2 FADE_RIGHT;
uniform vec2 FADE_UP;
uniform vec2 FADE_DOWN;

void main() {
    vec2 st = texCoord0;
    float alpha = 1.0;

    alpha = smoothstep(FADE_LEFT.x, FADE_LEFT.y, st.x);
    alpha *= smoothstep(FADE_RIGHT.x, FADE_RIGHT.y, 1. - st.x);
    alpha *= smoothstep(FADE_UP.x, FADE_UP.y, st.y);
    alpha *= smoothstep(FADE_DOWN.x, FADE_DOWN.y, 1. - st.y);

    if(u_invertAlpha == 1){
        alpha = 1.0 - alpha;
    }

    if(alpha <= 0.0){
        discard;
    }

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    color *= ColorModulator;
    color.a *= alpha;

    fragColor = color;

}
