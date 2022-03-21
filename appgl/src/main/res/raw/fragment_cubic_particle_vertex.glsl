// 支持实例化
#version 300 es
uniform mat4 uMVPMatrix;
in vec3 aPosition;
in vec2 aTexCoords;
//layout (location = 3) in mat4 aInstanceMatrix;//实例化数组 -法3，使用矩阵，支持旋转
layout(location = 3) in vec3 aOffset; //法4 使用向量-只支持平移
out vec2 TexCoord;


void main() {
    // 法3 实例化数组
//    gl_Position = uMVPMatrix * aInstanceMatrix * vec4(aPosition,1.0);
//    gl_Position = uMVPMatrix * vec4(aPosition,1.0);
    // 法4 实例数组，偏移量
//    gl_Position = uMVPMatrix * vec4(aPosition - vec3(0.0, 0.95, 0.0) + aOffset, 1.0);
    gl_Position = uMVPMatrix * vec4(aPosition+aOffset, 1.0);

    TexCoord = aTexCoords;
}