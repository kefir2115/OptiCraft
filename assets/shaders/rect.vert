#version 330 core

layout(location = 0) in vec2 aPos;

uniform vec2 uPosition;
uniform vec2 uSize;

void main() {
    vec2 scaled = aPos * uSize;
    vec2 translated = scaled + uPosition;
    gl_Position = vec4(translated, 0.0, 1.0);
}