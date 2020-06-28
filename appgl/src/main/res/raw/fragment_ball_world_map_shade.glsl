#version 300 es

precision highp float;
uniform sampler2D uTexture;
in vec2 vCoordinate;
out vec4 vFragColor;
void main(){
     vec4 color=texture(uTexture,vCoordinate);
     vFragColor=color;
}