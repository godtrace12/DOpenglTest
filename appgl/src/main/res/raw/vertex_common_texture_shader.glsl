#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 aTextureCoord;
uniform mat4 vMatrix;
out vec2 vTexCoord;
varying   vec2 textureCoordinate; //纹理坐标点变换后输出
void main() {
     gl_Position  = vMatrix * vPosition;
     gl_PointSize = 10.0;
     vTexCoord = aTextureCoord;
}