#version 320 es
precision mediump float;

uniform sampler2D gColorMap;
in vec2 TexCoord;
out vec4 FragColor;

void main(){
    FragColor = texture(gColorMap, TexCoord);
}