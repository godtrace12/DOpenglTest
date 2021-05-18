uniform mat4 textureTransform;
attribute vec4 vPosition; // 顶点坐标
attribute vec2 vTextureCoord;  //纹理坐标
varying   vec2 aCoord; //纹理坐标点变换后输出
uniform mat4 vMatrix;

 void main() {
//     gl_Position = vMatrix * vPosition;
////     gl_Position = vPosition;
//     aCoord = vTextureCoord;
     gl_Position = vPosition;
     aCoord = (vMatrix * vec4(vTextureCoord,1.0,1.0)).xy;
 }