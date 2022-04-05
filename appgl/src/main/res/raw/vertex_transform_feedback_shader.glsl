#version 300 es
layout (location = 0) in vec3 vPosition;
layout (location = 1) in vec2 aTextureCoord;
uniform mat4 vMatrix;
out vec2 vTexCoord;
out vec3 outPos;
out vec2 outTex;
void main() {
     gl_Position  = vMatrix * vec4(vPosition,0);
     vTexCoord = aTextureCoord;
     outPos = vPosition*3.0;
     outTex = aTextureCoord*3.0;
//     outPos = vPosition;
//     outTex = aTextureCoord;
}