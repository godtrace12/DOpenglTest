uniform mat4 textureTransform;
attribute vec2 aTextureCoord;
attribute vec4 vPosition;            //NDK坐标点
varying   vec2 textureCoordinate; //纹理坐标点变换后输出
uniform mat4 vMatrix;

 void main() {
     gl_Position = vMatrix * vPosition;
     textureCoordinate = aTextureCoord;
 }