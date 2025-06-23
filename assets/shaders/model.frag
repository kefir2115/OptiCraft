#version 330 core

in vec2 TexCoord;
in vec3 Normal;
in vec3 FragPos;

out vec4 FragColor;

uniform sampler2D texture0;

void main() {
    FragColor = vec4(1.0, 1.0, 1.0, 1.0);
}