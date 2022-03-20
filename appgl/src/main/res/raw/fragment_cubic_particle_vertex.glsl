// 支持实例化
#version 300 es
uniform mat4 uMVPMatrix;
in vec3 aPosition;
in vec2 aTexCoords;
layout (location = 3) in mat4 aInstanceMatrix;//实例化数组

out vec2 TexCoord;
uniform vec2 offsets[2];


void main() {
    // 法3 实例化数组
    gl_Position = uMVPMatrix * aInstanceMatrix * vec4(aPosition,1.0);
//    gl_Position = uMVPMatrix * vec4(aPosition,1.0);
    TexCoord = aTexCoords;
}