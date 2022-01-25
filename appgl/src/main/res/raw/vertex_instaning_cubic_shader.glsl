#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 aTextureCoord;
layout (location = 2) in mat4 aInstanceMatrix;//实例化数组

uniform mat4 vMatrix;
out vec2 vTexCoord;
void main() {
     gl_Position  = vMatrix * vPosition;
     // 1、实例化
//     gl_Position  = vMatrix * aInstanceMatrix * vPosition;
     gl_PointSize = 10.0;
     vTexCoord = aTextureCoord;
}