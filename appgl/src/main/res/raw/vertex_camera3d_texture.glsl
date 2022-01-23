#version 300 es
uniform mat4 textureTransform;
in vec2 inputTextureCoordinate;
in vec3 position;            //NDK坐标点
out vec2 textureCoordinate; //纹理坐标点变换后输出

 void main() {
//     gl_Position = position;

     vec4 pos = vec4(position, 1.0);

     gl_Position = pos.xyww;
     textureCoordinate = inputTextureCoordinate;
 }