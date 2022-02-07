#version 320 es
layout (location = 0) in vec4 vPosition;
//layout (location = 1) in vec3 aColor;
layout (location = 1) in vec3 aTextureCoord;//颜色坐标
uniform mat4 vMatrix;
//out vec2 vTexCoord;

out VS_OUT {
     vec3 color;
} vs_out;

void main() {
     gl_Position  = vMatrix * vPosition;
//     vTexCoord = aTextureCoord;
     vs_out.color = aTextureCoord;
}