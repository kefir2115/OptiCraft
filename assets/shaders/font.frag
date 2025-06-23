#version 330 core
in vec2 TexCoord;
out vec4 FragColor;
uniform vec4 textColor;

uniform sampler2D fontTexture;

void main() {
	float alpha = texture(fontTexture, TexCoord).r;
	FragColor = vec4(textColor.r, textColor.g, textColor.b, textColor.a * alpha);
}